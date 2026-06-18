package com.nun.aitestcase.service.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nun.aitestcase.vo.GeneratedPlanVO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GeneratedPlanParserTest {

    @Test
    void parsesFencedDeepSeekJsonIntoGeneratedPlan() {
        String content = """
                ```json
                {
                  "title": "Web 登录模块测试用例",
                  "summary": "覆盖正常登录、异常输入和安全场景。",
                  "scope": ["被测模块：登录模块", "目标平台：Web 浏览器"],
                  "risks": ["错误提示不能暴露敏感信息"],
                  "testCases": [
                    {
                      "id": "TC-001",
                      "title": "正常登录",
                      "priority": "P0",
                      "category": "正常场景",
                      "precondition": "用户已注册",
                      "steps": ["打开登录页", "输入正确账号密码", "点击登录"],
                      "expectedResult": "登录成功并跳转首页"
                    }
                  ]
                }
                ```
                """;

        GeneratedPlanParser parser = new GeneratedPlanParser(new ObjectMapper());

        GeneratedPlanVO plan = parser.parse(content, "登录模块", "deepseek-chat");

        assertEquals("Web 登录模块测试用例", plan.getTitle());
        assertEquals("deepseek-chat", plan.getUsedModel());
        assertEquals(2, plan.getScope().size());
        assertEquals(1, plan.getTestCases().size());
        assertEquals("TC-001", plan.getTestCases().get(0).getId());
        assertEquals("登录成功并跳转首页", plan.getTestCases().get(0).getExpectedResult());
    }
}
