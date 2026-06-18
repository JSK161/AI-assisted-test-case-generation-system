package com.nun.aitestcase.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nun.aitestcase.common.BusinessException;
import com.nun.aitestcase.dto.ProjectCreateRequest;
import com.nun.aitestcase.entity.Project;
import com.nun.aitestcase.mapper.ProjectMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProjectService {

    private final ProjectMapper projectMapper;

    public ProjectService(ProjectMapper projectMapper) {
        this.projectMapper = projectMapper;
    }

    public Project create(ProjectCreateRequest request) {
        Project project = new Project();
        project.setName(request.getName().trim());
        project.setDescription(request.getDescription());
        project.setCreatorId(request.getCreatorId() == null ? 1L : request.getCreatorId());
        project.setCreatedAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());
        projectMapper.insert(project);
        return project;
    }

    public List<Project> list() {
        return projectMapper.selectList(new LambdaQueryWrapper<Project>().orderByDesc(Project::getCreatedAt));
    }

    public Project getById(Long id) {
        Project project = projectMapper.selectById(id);
        if (project == null) {
            throw new BusinessException("Project does not exist");
        }
        return project;
    }
}
