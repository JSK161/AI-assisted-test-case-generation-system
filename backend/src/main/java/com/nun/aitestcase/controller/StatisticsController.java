package com.nun.aitestcase.controller;

import com.nun.aitestcase.common.ApiResponse;
import com.nun.aitestcase.service.StatisticsService;
import com.nun.aitestcase.vo.ProjectStatisticsVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/project/{projectId}")
    public ApiResponse<ProjectStatisticsVO> getProjectStatistics(@PathVariable Long projectId) {
        return ApiResponse.success(statisticsService.getProjectStatistics(projectId));
    }
}
