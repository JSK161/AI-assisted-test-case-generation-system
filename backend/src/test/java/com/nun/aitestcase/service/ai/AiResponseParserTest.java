package com.nun.aitestcase.service.ai;

import com.nun.aitestcase.dto.GeneratedTestCaseDTO;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AiResponseParserTest {

    @Test
    void parsesDeepSeekJsonIntoGeneratedTestCases() {
        String json = """
                {
                  "testCases": [
                    {
                      "title": "Login success",
                      "type": "Normal",
                      "precondition": "Registered user exists",
                      "steps": ["Open login page", "Input valid account", "Click login"],
                      "testData": "phone=13800000000,password=123456",
                      "expectedResult": "Login succeeds",
                      "priority": "High"
                    }
                  ]
                }
                """;

        AiResponseParser parser = new AiResponseParser();

        List<GeneratedTestCaseDTO> result = parser.parseTestCases(json);

        assertEquals(1, result.size());
        assertEquals("Login success", result.get(0).getTitle());
        assertEquals(3, result.get(0).getSteps().size());
        assertEquals("High", result.get(0).getPriority());
    }
}
