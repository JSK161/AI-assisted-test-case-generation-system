package com.nun.aitestcase.service;

import com.nun.aitestcase.entity.TestCase;
import com.nun.aitestcase.mapper.TestCaseMapper;
import com.nun.aitestcase.vo.ProjectStatisticsVO;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StatisticsServiceTest {

    @Test
    void calculateProjectStatisticsCountsStatusesAndAdoptionRate() {
        TestCaseMapper mapper = mock(TestCaseMapper.class);
        when(mapper.selectByProjectId(1L)).thenReturn(List.of(
                testCase("ADOPTED"),
                testCase("ADOPTED"),
                testCase("NEEDS_REVISION"),
                testCase("REJECTED")
        ));
        StatisticsService service = new StatisticsService(mapper);

        ProjectStatisticsVO result = service.getProjectStatistics(1L);

        assertEquals(4, result.getTotalCount());
        assertEquals(2, result.getAdoptedCount());
        assertEquals(1, result.getNeedsRevisionCount());
        assertEquals(1, result.getRejectedCount());
        assertEquals(50.0, result.getAdoptionRate());
    }

    @Test
    void calculateProjectStatisticsReturnsZeroRateWhenNoCasesExist() {
        TestCaseMapper mapper = mock(TestCaseMapper.class);
        when(mapper.selectByProjectId(1L)).thenReturn(List.of());
        StatisticsService service = new StatisticsService(mapper);

        ProjectStatisticsVO result = service.getProjectStatistics(1L);

        assertEquals(0, result.getTotalCount());
        assertEquals(0.0, result.getAdoptionRate());
    }

    private TestCase testCase(String status) {
        TestCase testCase = new TestCase();
        testCase.setAdoptionStatus(status);
        return testCase;
    }
}
