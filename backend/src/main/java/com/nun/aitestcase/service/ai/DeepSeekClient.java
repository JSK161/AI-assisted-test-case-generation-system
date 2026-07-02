package com.nun.aitestcase.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nun.aitestcase.common.BusinessException;
import com.nun.aitestcase.config.DeepSeekProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
public class DeepSeekClient {

    private final DeepSeekProperties properties;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;
    private static final ThreadLocal<String> userApiKeyHolder = new ThreadLocal<>();

    public DeepSeekClient(DeepSeekProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder().build();
    }

    public static void setUserApiKey(String apiKey) {
        userApiKeyHolder.set(apiKey);
    }

    public static void clearUserApiKey() {
        userApiKeyHolder.remove();
    }

    public static boolean hasUserApiKey() {
        return StringUtils.hasText(userApiKeyHolder.get());
    }

    private String resolveApiKey() {
        String userKey = userApiKeyHolder.get();
        if (StringUtils.hasText(userKey)) return userKey;
        return properties.getApiKey();
    }

    public String generateTestCases(String prompt) {
        String apiKey = resolveApiKey();
        // Skip mock if user has their own API key
        if (properties.isMockEnabled() && !StringUtils.hasText(userApiKeyHolder.get())) {
            return mockResponse();
        }
        if (!StringUtils.hasText(apiKey)) {
            throw new BusinessException("DeepSeek API key is not configured");
        }

        Map<String, Object> body = Map.of(
                "model", properties.getModel(),
                "temperature", 0.2,
                "messages", List.of(
                        Map.of("role", "system", "content", "You are a professional software testing engineer."),
                        Map.of("role", "user", "content", prompt)
                )
        );

        String rawResponse = restClient.post()
                .uri(properties.getBaseUrl() + "/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + resolveApiKey())
                .body(body)
                .retrieve()
                .body(String.class);

        try {
            JsonNode root = objectMapper.readTree(rawResponse);
            JsonNode content = root.path("choices").path(0).path("message").path("content");
            if (!content.isTextual()) {
                throw new BusinessException("DeepSeek response content is empty");
            }
            return content.asText();
        } catch (Exception exception) {
            throw new BusinessException("Failed to parse DeepSeek response");
        }
    }

    private String mockResponse() {
        return """
                {
                  "testCases": [
                    {
                      "title": "登录成功测试",
                      "type": "正常场景",
                      "precondition": "用户已注册账号，系统服务正常",
                      "steps": ["进入登录页面", "输入正确手机号、密码和验证码", "点击登录按钮"],
                      "testData": "手机号：13800000000；密码：123456；验证码：8888",
                      "expectedResult": "系统登录成功并跳转到首页",
                      "priority": "高"
                    },
                    {
                      "title": "密码错误时登录失败",
                      "type": "异常场景",
                      "precondition": "用户已注册账号",
                      "steps": ["进入登录页面", "输入正确手机号", "输入错误密码", "点击登录按钮"],
                      "testData": "手机号：13800000000；密码：wrong123",
                      "expectedResult": "系统提示账号或密码错误，用户停留在登录页",
                      "priority": "高"
                    },
                    {
                      "title": "手机号为空时提示必填",
                      "type": "边界场景",
                      "precondition": "用户打开登录页面",
                      "steps": ["清空手机号输入框", "输入密码和验证码", "点击登录按钮"],
                      "testData": "手机号为空；密码：123456；验证码：8888",
                      "expectedResult": "系统提示手机号不能为空",
                      "priority": "中"
                    }
                  ]
                }
                """;
    }
}
