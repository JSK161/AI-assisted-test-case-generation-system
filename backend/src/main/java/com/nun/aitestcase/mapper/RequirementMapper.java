package com.nun.aitestcase.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nun.aitestcase.entity.Requirement;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface RequirementMapper extends BaseMapper<Requirement> {

    @Select("select id, project_id, title, content, created_by, created_at, updated_at from requirement where project_id = #{projectId} order by created_at desc")
    List<Requirement> selectByProjectId(Long projectId);
}
