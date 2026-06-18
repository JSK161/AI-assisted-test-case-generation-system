package com.nun.aitestcase.service;

import com.nun.aitestcase.common.BusinessException;
import com.nun.aitestcase.dto.RequirementCreateRequest;
import com.nun.aitestcase.entity.Requirement;
import com.nun.aitestcase.mapper.ProjectMapper;
import com.nun.aitestcase.mapper.RequirementMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RequirementService {

    private final RequirementMapper requirementMapper;
    private final ProjectMapper projectMapper;

    public RequirementService(RequirementMapper requirementMapper, ProjectMapper projectMapper) {
        this.requirementMapper = requirementMapper;
        this.projectMapper = projectMapper;
    }

    public Requirement create(RequirementCreateRequest request) {
        if (projectMapper.selectById(request.getProjectId()) == null) {
            throw new BusinessException("Project does not exist");
        }
        Requirement requirement = new Requirement();
        requirement.setProjectId(request.getProjectId());
        requirement.setTitle(request.getTitle().trim());
        requirement.setContent(request.getContent().trim());
        requirement.setCreatedBy(request.getCreatedBy() == null ? 1L : request.getCreatedBy());
        requirement.setCreatedAt(LocalDateTime.now());
        requirement.setUpdatedAt(LocalDateTime.now());
        requirementMapper.insert(requirement);
        return requirement;
    }

    public List<Requirement> listByProjectId(Long projectId) {
        return requirementMapper.selectByProjectId(projectId);
    }
}
