package com.nun.aitestcase.service.ai;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestionPromptBuilderTest {

    @Test
    void buildsPromptAskingDeepSeekForRequirementSpecificQuestions() {
        QuestionPromptBuilder builder = new QuestionPromptBuilder();

        String prompt = builder.build("我想测试订单退款模块", null, "退款规则：原路退回，超过 24 小时需要人工审核。");

        assertTrue(prompt.contains("订单退款模块"));
        assertTrue(prompt.contains("原路退回"));
        assertTrue(prompt.contains("\"questions\""));
        assertTrue(prompt.contains("\"type\""));
        assertTrue(prompt.contains("不要输出固定模板问题"));
        assertTrue(prompt.contains("反问"));
    }
}
