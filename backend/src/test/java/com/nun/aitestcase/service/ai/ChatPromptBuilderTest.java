package com.nun.aitestcase.service.ai;

import com.nun.aitestcase.dto.ChatAnswerDTO;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ChatPromptBuilderTest {

    @Test
    void buildsPromptWithRequirementAnswersReferenceAndJsonContract() {
        ChatAnswerDTO answer = new ChatAnswerDTO();
        answer.setQuestionId("platform");
        answer.setValues(List.of("Web 浏览器"));
        answer.setCustomText("Chrome、Edge");

        ChatPromptBuilder builder = new ChatPromptBuilder();

        String prompt = builder.build("我想测试登录模块", List.of(answer), "参考页面要求：账号连续失败 5 次锁定。");

        assertTrue(prompt.contains("我想测试登录模块"));
        assertTrue(prompt.contains("platform"));
        assertTrue(prompt.contains("Web 浏览器"));
        assertTrue(prompt.contains("Chrome、Edge"));
        assertTrue(prompt.contains("账号连续失败 5 次锁定"));
        assertTrue(prompt.contains("\"testCases\""));
        assertTrue(prompt.contains("\"expectedResult\""));
    }
}
