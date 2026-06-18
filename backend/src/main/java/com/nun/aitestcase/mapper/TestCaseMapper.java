package com.nun.aitestcase.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nun.aitestcase.entity.TestCase;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface TestCaseMapper extends BaseMapper<TestCase> {

    @Select("select id, project_id, requirement_id, title, type, precondition, steps, test_data, expected_result, priority, adoption_status, created_at, updated_at from test_case where project_id = #{projectId} order by created_at desc")
    List<TestCase> selectByProjectId(Long projectId);
}
