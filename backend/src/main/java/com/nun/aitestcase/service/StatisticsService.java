package com.nun.aitestcase.service;

import com.nun.aitestcase.common.AdoptionStatus;
import com.nun.aitestcase.entity.TestCase;
import com.nun.aitestcase.mapper.TestCaseMapper;
import com.nun.aitestcase.vo.ProjectStatisticsVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StatisticsService {

    private final TestCaseMapper testCaseMapper;

    public StatisticsService(TestCaseMapper testCaseMapper) {
        this.testCaseMapper = testCaseMapper;
    }

    public ProjectStatisticsVO getProjectStatistics(Long projectId) {
        List<TestCase> testCases = testCaseMapper.selectByProjectId(projectId);
        int total = testCases.size();
        int adopted = countByStatus(testCases, AdoptionStatus.ADOPTED);
        int needsRevision = countByStatus(testCases, AdoptionStatus.NEEDS_REVISION);
        int rejected = countByStatus(testCases, AdoptionStatus.REJECTED);
        int pending = countByStatus(testCases, AdoptionStatus.PENDING);

        ProjectStatisticsVO vo = new ProjectStatisticsVO();
        vo.setTotalCount(total);
        vo.setAdoptedCount(adopted);
        vo.setNeedsRevisionCount(needsRevision);
        vo.setRejectedCount(rejected);
        vo.setPendingCount(pending);
        vo.setAdoptionRate(total == 0 ? 0.0 : adopted * 100.0 / total);
        return vo;
    }

    private int countByStatus(List<TestCase> testCases, String status) {
        return (int) testCases.stream()
                .filter(testCase -> status.equals(testCase.getAdoptionStatus()))
                .count();
    }
}
