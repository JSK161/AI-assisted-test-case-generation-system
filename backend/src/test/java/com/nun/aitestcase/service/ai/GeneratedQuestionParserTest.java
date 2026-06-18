package com.nun.aitestcase.service.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nun.aitestcase.vo.GeneratedQuestionResponseVO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GeneratedQuestionParserTest {

    @Test
    void parsesFencedDynamicQuestionJsonAndAppendsConfirmQuestion() {
        String content = """
                ```json
                {
                  "questions": [
                    {
                      "id": "refund_type",
                      "title": "退款模块需要覆盖哪些退款类型？",
                      "type": "multiple",
                      "allowCustom": true,
                      "options": [
                        {"label": "仅退款", "value": "仅退款", "description": "未发货或未收货场景"},
                        {"label": "退货退款", "value": "退货退款", "description": "涉及物流和验收"}
                      ]
                    }
                  ]
                }
                ```
                """;

        GeneratedQuestionParser parser = new GeneratedQuestionParser(new ObjectMapper());

        GeneratedQuestionResponseVO response = parser.parse(content, "deepseek-chat");

        assertEquals("deepseek-chat", response.getUsedModel());
        assertEquals(2, response.getQuestions().size());
        assertEquals("refund_type", response.getQuestions().get(0).getId());
        assertEquals("退款模块需要覆盖哪些退款类型？", response.getQuestions().get(0).getTitle());
        assertEquals("multiple", response.getQuestions().get(0).getType());
        assertEquals("仅退款", response.getQuestions().get(0).getOptions().get(0).getValue());
        assertEquals("confirm", response.getQuestions().get(1).getId());
    }
}
