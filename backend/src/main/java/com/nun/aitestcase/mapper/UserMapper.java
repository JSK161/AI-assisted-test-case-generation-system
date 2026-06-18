package com.nun.aitestcase.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nun.aitestcase.entity.User;
import org.apache.ibatis.annotations.Select;

public interface UserMapper extends BaseMapper<User> {

    @Select("select id, username, password, real_name, role, created_at, updated_at from sys_user where username = #{username} limit 1")
    User selectByUsername(String username);
}
