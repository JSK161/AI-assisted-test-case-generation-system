package com.nun.aitestcase.service.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nun.aitestcase.common.AdoptionStatus;
import com.nun.aitestcase.common.BusinessException;
import com.nun.aitestcase.config.DeepSeekProperties;
import com.nun.aitestcase.dto.GenerateTestCasesRequest;
import com.nun.aitestcase.dto.GeneratedTestCaseDTO;
import com.nun.aitestcase.entity.AiGenerationRecord;
import com.nun.aitestcase.entity.Requirement;
import com.nun.aitestcase.entity.TestCase;
import com.nun.aitestcase.mapper.AiGenerationRecordMapper;
import com.nun.aitestcase.mapper.ProjectMapper;
import com.nun.aitestcase.mapper.RequirementMapper;
import com.nun.aitestcase.mapper.TestCaseMapper;
import com.nun.aitestcase.service.TestCaseService;
import com.nun.aitestcase.vo.TestCaseVO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AiGenerationService {

    private final ProjectMapper projectMapper;
    private final RequirementMapper requirementMapper;
    private final TestCaseMapper testCaseMapper;
    private final AiGenerationRecordMapper recordMapper;
    private final DeepSeekClient deepSeekClient;
    private final AiResponseParser responseParser;
    private final DeepSeekProperties properties;
    private final ObjectMapper objectMapper;
    private final TestCaseService testCaseService;

    public AiGenerationService(
            ProjectMapper projectMapper,
            RequirementMapper requirementMapper,
            TestCaseMapper testCaseMapper,
            AiGenerationRecordMapper recordMapper,
            DeepSeekClient deepSeekClient,
            AiResponseParser responseParser,
            DeepSeekProperties properties,
            ObjectMapper objectMapper,
            TestCaseService testCaseService
    ) {
        this.projectMapper = projectMapper;
        this.requirementMapper = requirementMapper;
        this.testCaseMapper = testCaseMapper;
        this.recordMapper = recordMapper;
        this.deepSeekClient = deepSeekClient;
        this.responseParser = responseParser;
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.testCaseService = testCaseService;
    }

    public List<TestCaseVO> generate(GenerateTestCasesRequest request) {
        if (projectMapper.selectById(request.getProjectId()) == null) {
            throw new BusinessException("Project does not exist");
        }
        Requirement requirement = requirementMapper.selectById(request.getRequirementId());
        if (requirement == null || !request.getProjectId().equals(requirement.getProjectId())) {
            throw new BusinessException("Requirement does not exist in this project");
        }

        String prompt = buildPrompt(requirement);
        AiGenerationRecord record = createRecord(request, prompt);
        try {
            String response = deepSeekClient.generateTestCases(prompt);
            List<GeneratedTestCaseDTO> generated = responseParser.parseTestCases(response);
            List<TestCaseVO> saved = generated.stream()
                    .map(dto -> saveTestCase(request, dto))
                    .map(testCaseService::toVO)
                    .toList();
            record.setResponseContent(response);
            record.setStatus("SUCCESS");
            recordMapper.insert(record);
            return saved;
        } catch (RuntimeException exception) {
            record.setStatus("FAILED");
            record.setErrorMessage(exception.getMessage());
            recordMapper.insert(record);
            throw exception;
        }
    }

    private String buildPrompt(Requirement requirement) {
        return """
                你是一名软件测试工程师。请根据以下需求生成测试用例。
                要求：
                1. 覆盖正常场景、异常场景和边界场景。
                2. 返回 JSON，不要返回 Markdown。
                3. JSON 根字段必须是 testCases。
                4. 每条用例字段包括 title、type、precondition、steps、testData、expectedResult、priority。
                5. steps 必须是字符串数组。

                需求标题：%s
                需求内容：%s
                """.formatted(requirement.getTitle(), requirement.getContent());
    }

    private AiGenerationRecord createRecord(GenerateTestCasesRequest request, String prompt) {
        AiGenerationRecord record = new AiGenerationRecord();
        record.setProjectId(request.getProjectId());
        record.setRequirementId(request.getRequirementId());
        record.setModelName(properties.isMockEnabled() ? "mock-deepseek" : properties.getModel());
        record.setPrompt(prompt);
        record.setCreatedAt(LocalDateTime.now());
        return record;
    }

    private TestCase saveTestCase(GenerateTestCasesRequest request, GeneratedTestCaseDTO dto) {
        TestCase testCase = new TestCase();
        testCase.setProjectId(request.getProjectId());
        testCase.setRequirementId(request.getRequirementId());
        testCase.setTitle(defaultText(dto.getTitle(), "未命名测试用例"));
        testCase.setType(defaultText(dto.getType(), "功能场景"));
        testCase.setPrecondition(defaultText(dto.getPrecondition(), "无"));
        testCase.setSteps(writeSteps(dto.getSteps()));
        testCase.setTestData(defaultText(dto.getTestData(), "无"));
        testCase.setExpectedResult(defaultText(dto.getExpectedResult(), "符合需求预期"));
        testCase.setPriority(defaultText(dto.getPriority(), "中"));
        testCase.setAdoptionStatus(AdoptionStatus.PENDING);
        testCase.setCreatedAt(LocalDateTime.now());
        testCase.setUpdatedAt(LocalDateTime.now());
        testCaseMapper.insert(testCase);
        return testCase;
    }

    private String writeSteps(List<String> steps) {
        try {
            return objectMapper.writeValueAsString(steps == null ? List.of() : steps);
        } catch (JsonProcessingException exception) {
            throw new BusinessException("Test steps format is invalid");
        }
    }

    private String defaultText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
