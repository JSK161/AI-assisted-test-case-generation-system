package com.nun.aitestcase.service;

import com.nun.aitestcase.common.BusinessException;
import com.nun.aitestcase.entity.User;
import com.nun.aitestcase.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AuthService {

    private final UserMapper userMapper;

    public AuthService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public User login(String username, String password) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            throw new BusinessException("Username and password are required");
        }
        User user = userMapper.selectByUsername(username.trim());
        if (user == null || !password.equals(user.getPassword())) {
            throw new BusinessException("Username or password is incorrect");
        }
        return user;
    }
}
