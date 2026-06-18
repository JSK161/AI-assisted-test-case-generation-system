package com.nun.aitestcase.service.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nun.aitestcase.common.BusinessException;
import com.nun.aitestcase.dto.GeneratedTestCaseDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AiResponseParser {

    private final ObjectMapper objectMapper;

    public AiResponseParser() {
        this(new ObjectMapper());
    }

    public AiResponseParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<GeneratedTestCaseDTO> parseTestCases(String responseContent) {
        try {
            String json = extractJson(responseContent);
            JsonNode root = objectMapper.readTree(json);
            JsonNode testCases = root.get("testCases");
            if (testCases == null || !testCases.isArray()) {
                throw new BusinessException("AI response does not contain testCases array");
            }
            List<GeneratedTestCaseDTO> result = objectMapper.readValue(
                    testCases.toString(),
                    new TypeReference<List<GeneratedTestCaseDTO>>() {
                    }
            );
            if (result.isEmpty()) {
                throw new BusinessException("AI response contains no test cases");
            }
            return result;
        } catch (JsonProcessingException exception) {
            throw new BusinessException("AI response format is invalid");
        }
    }

    private String extractJson(String content) {
        if (content == null || content.isBlank()) {
            throw new BusinessException("AI response is empty");
        }
        String cleaned = content.trim()
                .replace("```json", "")
                .replace("```JSON", "")
                .replace("```", "")
                .trim();
        int start = cleaned.indexOf('{');
        int end = cleaned.lastIndexOf('}');
        if (start < 0 || end < start) {
            throw new BusinessException("AI response format is invalid");
        }
        return cleaned.substring(start, end + 1);
    }
}
