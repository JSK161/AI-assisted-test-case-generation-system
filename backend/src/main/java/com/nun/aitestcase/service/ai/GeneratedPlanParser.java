package com.nun.aitestcase.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nun.aitestcase.common.BusinessException;
import com.nun.aitestcase.vo.GeneratedPlanVO;
import com.nun.aitestcase.vo.TestCasePlanItemVO;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Component
public class GeneratedPlanParser {

    private final ObjectMapper objectMapper;

    public GeneratedPlanParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public GeneratedPlanVO parse(String content, String requirement, String usedModel) {
        try {
            JsonNode root = objectMapper.readTree(extractJson(content));
            GeneratedPlanVO plan = new GeneratedPlanVO();
            plan.setTitle(text(root, "title", extractModuleName(requirement) + "测试用例方案"));
            plan.setSummary(text(root, "summary", requirement));
            plan.setScope(stringList(root.path("scope")));
            plan.setRisks(stringList(root.path("risks")));
            plan.setTestCases(parseTestCases(root.path("testCases")));
            plan.setUsedModel(usedModel);
            fillDefaults(plan, requirement);
            return plan;
        } catch (Exception exception) {
            throw new BusinessException("DeepSeek 返回内容不是可解析的测试方案 JSON");
        }
    }

    private String extractJson(String content) {
        if (!StringUtils.hasText(content)) {
            throw new BusinessException("DeepSeek 返回内容为空");
        }
        int start = content.indexOf('{');
        int end = content.lastIndexOf('}');
        if (start < 0 || end <= start) {
            throw new BusinessException("DeepSeek 返回内容缺少 JSON 对象");
        }
        return content.substring(start, end + 1);
    }

    private List<TestCasePlanItemVO> parseTestCases(JsonNode node) {
        List<TestCasePlanItemVO> result = new ArrayList<>();
        if (!node.isArray()) {
            return result;
        }

        int index = 1;
        for (JsonNode item : node) {
            TestCasePlanItemVO testCase = new TestCasePlanItemVO();
            testCase.setId(text(item, "id", "TC-%03d".formatted(index)));
            testCase.setTitle(text(item, "title", "测试用例 " + index));
            testCase.setPriority(normalizePriority(text(item, "priority", "P1")));
            testCase.setCategory(text(item, "category", text(item, "type", "功能场景")));
            testCase.setPrecondition(text(item, "precondition", "测试环境可访问，测试数据已准备。"));
            testCase.setSteps(stringList(item.path("steps")));
            testCase.setExpectedResult(text(item, "expectedResult", "系统响应符合需求预期。"));
            if (testCase.getSteps().isEmpty()) {
                testCase.setSteps(List.of("进入被测功能入口", "按用例数据执行操作", "观察页面、接口返回和业务状态"));
            }
            result.add(testCase);
            index++;
        }
        return result;
    }

    private void fillDefaults(GeneratedPlanVO plan, String requirement) {
        String moduleName = extractModuleName(requirement);
        if (plan.getScope().isEmpty()) {
            plan.setScope(List.of(
                    "被测模块：" + moduleName,
                    "测试类型：功能测试、异常测试、边界值测试、安全测试、兼容性测试"
            ));
        }
        if (plan.getRisks().isEmpty()) {
            plan.setRisks(List.of(
                    "需求约束不完整时，需要人工确认字段规则、权限边界和异常提示。",
                    "状态类逻辑需要重点验证刷新、回退、重复提交和会话过期。"
            ));
        }
        if (plan.getTestCases().isEmpty()) {
            TestCasePlanItemVO testCase = new TestCasePlanItemVO();
            testCase.setId("TC-001");
            testCase.setTitle(moduleName + "正常流程验证");
            testCase.setPriority("P0");
            testCase.setCategory("正常场景");
            testCase.setPrecondition("测试环境可访问，测试数据已准备。");
            testCase.setSteps(List.of("进入被测功能入口", "输入合法数据", "提交操作", "观察处理结果"));
            testCase.setExpectedResult("系统处理成功，页面状态和业务数据符合需求预期。");
            plan.setTestCases(List.of(testCase));
        }
    }

    private String text(JsonNode node, String field, String fallback) {
        JsonNode value = node.path(field);
        if (value.isTextual() && StringUtils.hasText(value.asText())) {
            return value.asText();
        }
        return fallback;
    }

    private List<String> stringList(JsonNode node) {
        List<String> values = new ArrayList<>();
        if (node.isArray()) {
            for (JsonNode item : node) {
                if (item.isTextual() && StringUtils.hasText(item.asText())) {
                    values.add(item.asText());
                }
            }
        } else if (node.isTextual() && StringUtils.hasText(node.asText())) {
            values.add(node.asText());
        }
        return values;
    }

    private String normalizePriority(String priority) {
        String normalized = priority.toUpperCase();
        if (normalized.contains("P0") || normalized.contains("HIGH") || priority.contains("高")) {
            return "P0";
        }
        if (normalized.contains("P2") || normalized.contains("LOW") || priority.contains("低")) {
            return "P2";
        }
        return "P1";
    }

    private String extractModuleName(String requirement) {
        if (!StringUtils.hasText(requirement)) {
            return "目标模块";
        }
        if (requirement.contains("登录")) {
            return "登录模块";
        }
        if (requirement.contains("接口")) {
            return "接口模块";
        }
        String normalized = requirement.replaceAll("[，。,.]", " ").trim();
        return normalized.length() > 16 ? normalized.substring(0, 16) : normalized;
    }
}
