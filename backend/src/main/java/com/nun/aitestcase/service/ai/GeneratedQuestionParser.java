package com.nun.aitestcase.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nun.aitestcase.common.BusinessException;
import com.nun.aitestcase.vo.GeneratedQuestionOptionVO;
import com.nun.aitestcase.vo.GeneratedQuestionResponseVO;
import com.nun.aitestcase.vo.GeneratedQuestionVO;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class GeneratedQuestionParser {

    private final ObjectMapper objectMapper;

    public GeneratedQuestionParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public GeneratedQuestionResponseVO parse(String content, String usedModel) {
        try {
            JsonNode root = objectMapper.readTree(extractJson(content));
            List<GeneratedQuestionVO> questions = parseQuestions(root.path("questions"));
            if (questions.isEmpty()) {
                throw new BusinessException("DeepSeek 没有返回可用的反问问题");
            }

            GeneratedQuestionResponseVO response = new GeneratedQuestionResponseVO();
            response.setQuestions(appendConfirmQuestion(questions));
            response.setUsedModel(usedModel);
            return response;
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BusinessException("DeepSeek 返回内容不是可解析的反问问题 JSON");
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

    private List<GeneratedQuestionVO> parseQuestions(JsonNode node) {
        List<GeneratedQuestionVO> questions = new ArrayList<>();
        if (!node.isArray()) {
            return questions;
        }

        int index = 1;
        for (JsonNode item : node) {
            GeneratedQuestionVO question = new GeneratedQuestionVO();
            question.setId(normalizeId(text(item, "id", "q" + index), index));
            question.setTitle(text(item, "title", "请补充第 " + index + " 个测试条件"));
            question.setType(normalizeType(text(item, "type", "single")));
            question.setAllowCustom(item.path("allowCustom").asBoolean(true));
            question.setOptions(parseOptions(item.path("options")));
            if (!question.getOptions().isEmpty() && !"confirm".equals(question.getId())) {
                questions.add(question);
                index++;
            }
            if (questions.size() >= 5) {
                break;
            }
        }
        return questions;
    }

    private List<GeneratedQuestionOptionVO> parseOptions(JsonNode node) {
        List<GeneratedQuestionOptionVO> options = new ArrayList<>();
        if (!node.isArray()) {
            return options;
        }

        for (JsonNode item : node) {
            String label = text(item, "label", text(item, "value", ""));
            if (!StringUtils.hasText(label)) {
                continue;
            }
            GeneratedQuestionOptionVO option = new GeneratedQuestionOptionVO();
            option.setLabel(label);
            option.setValue(text(item, "value", label));
            option.setDescription(text(item, "description", ""));
            options.add(option);
            if (options.size() >= 5) {
                break;
            }
        }
        return options;
    }

    private List<GeneratedQuestionVO> appendConfirmQuestion(List<GeneratedQuestionVO> questions) {
        Set<String> ids = questions.stream().map(GeneratedQuestionVO::getId).collect(Collectors.toSet());
        if (ids.contains("confirm")) {
            return questions;
        }
        List<GeneratedQuestionVO> result = new ArrayList<>(questions);
        GeneratedQuestionOptionVO confirmOption = new GeneratedQuestionOptionVO();
        confirmOption.setLabel("确认生成");
        confirmOption.setValue("confirmed");
        confirmOption.setDescription("使用以上回答生成测试用例方案");

        GeneratedQuestionVO confirm = new GeneratedQuestionVO();
        confirm.setId("confirm");
        confirm.setTitle("总览并确认");
        confirm.setType("confirm");
        confirm.setAllowCustom(false);
        confirm.setOptions(List.of(confirmOption));
        result.add(confirm);
        return result;
    }

    private String text(JsonNode node, String field, String fallback) {
        JsonNode value = node.path(field);
        if (value.isTextual() && StringUtils.hasText(value.asText())) {
            return value.asText().trim();
        }
        return fallback;
    }

    private String normalizeId(String id, int index) {
        String normalized = id.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_\\-]", "_");
        if (!StringUtils.hasText(normalized)) {
            return "q" + index;
        }
        return normalized;
    }

    private String normalizeType(String type) {
        if ("multiple".equalsIgnoreCase(type)) {
            return "multiple";
        }
        if ("confirm".equalsIgnoreCase(type)) {
            return "confirm";
        }
        return "single";
    }
}
