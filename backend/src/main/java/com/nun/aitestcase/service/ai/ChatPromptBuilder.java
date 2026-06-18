package com.nun.aitestcase.service.ai;

import com.nun.aitestcase.dto.ChatAnswerDTO;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ChatPromptBuilder {

    public String build(String requirement, List<ChatAnswerDTO> answers, String referenceContent) {
        String answerText = summarizeAnswers(answers);
        String referenceText = StringUtils.hasText(referenceContent)
                ? referenceContent
                : "用户没有提供可读取的参考页面内容。";

        return """
                你是一名资深软件测试工程师，请根据用户需求生成结构化测试用例方案。
                输出必须是严格 JSON，不要使用 Markdown 代码块，不要添加 JSON 之外的解释文字。

                JSON 字段要求：
                {
                  "title": "方案标题",
                  "summary": "一段方案摘要",
                  "scope": ["被测模块：...", "目标平台：...", "测试类型：..."],
                  "risks": ["风险提示1", "风险提示2"],
                  "testCases": [
                    {
                      "id": "TC-001",
                      "title": "用例标题",
                      "priority": "P0/P1/P2",
                      "category": "正常场景/异常场景/边界场景/安全场景/兼容性场景",
                      "precondition": "前置条件",
                      "steps": ["步骤1", "步骤2"],
                      "expectedResult": "预期结果"
                    }
                  ]
                }

                生成规则：
                1. 至少生成 8 条测试用例，覆盖正常流程、异常输入、边界值、安全、兼容性和体验场景。
                2. 每条用例必须包含清晰步骤和可验证的预期结果。
                3. 优先级使用 P0、P1、P2，其中核心功能和安全问题用 P0/P1。
                4. 如果用户信息不足，主动按通用软件测试经验补齐合理假设。
                5. 如果参考页面内容里有约束、案例或规则，要吸收进 scope、risks 和 testCases。

                用户需求：
                %s

                用户补充选择：
                %s

                参考页面内容：
                %s
                """.formatted(requirement, answerText, referenceText);
    }

    public String summarizeAnswers(List<ChatAnswerDTO> answers) {
        if (answers == null || answers.isEmpty()) {
            return "用户未填写补充选项。";
        }

        return answers.stream()
                .filter(answer -> !"confirm".equals(answer.getQuestionId()))
                .map(this::formatAnswer)
                .collect(Collectors.joining("\n"));
    }

    private String formatAnswer(ChatAnswerDTO answer) {
        String values = answer.getValues() == null || answer.getValues().isEmpty()
                ? "未选择"
                : String.join("、", answer.getValues());
        if (StringUtils.hasText(answer.getCustomText())) {
            values = values + "；补充：" + answer.getCustomText().trim();
        }
        return "- " + answer.getQuestionId() + "：" + values;
    }
}
