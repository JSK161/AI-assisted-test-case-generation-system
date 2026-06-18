package com.nun.aitestcase.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nun.aitestcase.common.AdoptionStatus;
import com.nun.aitestcase.common.BusinessException;
import com.nun.aitestcase.dto.TestCaseUpdateRequest;
import com.nun.aitestcase.entity.TestCase;
import com.nun.aitestcase.mapper.TestCaseMapper;
import com.nun.aitestcase.vo.TestCaseVO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TestCaseService {

    private final TestCaseMapper testCaseMapper;
    private final ObjectMapper objectMapper;

    public TestCaseService(TestCaseMapper testCaseMapper, ObjectMapper objectMapper) {
        this.testCaseMapper = testCaseMapper;
        this.objectMapper = objectMapper;
    }

    public List<TestCaseVO> listByProjectId(Long projectId) {
        return testCaseMapper.selectByProjectId(projectId).stream()
                .map(this::toVO)
                .toList();
    }

    public TestCaseVO getById(Long id) {
        TestCase testCase = findById(id);
        return toVO(testCase);
    }

    public TestCaseVO update(Long id, TestCaseUpdateRequest request) {
        TestCase testCase = findById(id);
        testCase.setTitle(request.getTitle().trim());
        testCase.setType(request.getType());
        testCase.setPrecondition(request.getPrecondition());
        testCase.setSteps(writeSteps(request.getSteps()));
        testCase.setTestData(request.getTestData());
        testCase.setExpectedResult(request.getExpectedResult());
        testCase.setPriority(request.getPriority());
        if (request.getAdoptionStatus() != null) {
            validateStatus(request.getAdoptionStatus());
            testCase.setAdoptionStatus(request.getAdoptionStatus());
        }
        testCase.setUpdatedAt(LocalDateTime.now());
        testCaseMapper.updateById(testCase);
        return toVO(testCase);
    }

    public TestCaseVO updateAdoptionStatus(Long id, String adoptionStatus) {
        validateStatus(adoptionStatus);
        TestCase testCase = findById(id);
        testCase.setAdoptionStatus(adoptionStatus);
        testCase.setUpdatedAt(LocalDateTime.now());
        testCaseMapper.updateById(testCase);
        return toVO(testCase);
    }

    public TestCaseVO toVO(TestCase testCase) {
        TestCaseVO vo = new TestCaseVO();
        vo.setId(testCase.getId());
        vo.setProjectId(testCase.getProjectId());
        vo.setRequirementId(testCase.getRequirementId());
        vo.setTitle(testCase.getTitle());
        vo.setType(testCase.getType());
        vo.setPrecondition(testCase.getPrecondition());
        vo.setSteps(readSteps(testCase.getSteps()));
        vo.setTestData(testCase.getTestData());
        vo.setExpectedResult(testCase.getExpectedResult());
        vo.setPriority(testCase.getPriority());
        vo.setAdoptionStatus(testCase.getAdoptionStatus());
        vo.setCreatedAt(testCase.getCreatedAt());
        vo.setUpdatedAt(testCase.getUpdatedAt());
        return vo;
    }

    String writeSteps(List<String> steps) {
        try {
            return objectMapper.writeValueAsString(steps == null ? List.of() : steps);
        } catch (JsonProcessingException exception) {
            throw new BusinessException("Test steps format is invalid");
        }
    }

    private List<String> readSteps(String steps) {
        if (steps == null || steps.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(steps, new TypeReference<List<String>>() {
            });
        } catch (JsonProcessingException exception) {
            return List.of(steps);
        }
    }

    private TestCase findById(Long id) {
        TestCase testCase = testCaseMapper.selectById(id);
        if (testCase == null) {
            throw new BusinessException("Test case does not exist");
        }
        return testCase;
    }

    private void validateStatus(String adoptionStatus) {
        if (!AdoptionStatus.isValid(adoptionStatus)) {
            throw new BusinessException("Adoption status is invalid");
        }
    }
}
