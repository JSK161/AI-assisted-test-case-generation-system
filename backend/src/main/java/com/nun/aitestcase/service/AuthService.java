package com.nun.aitestcase.service;

import com.nun.aitestcase.common.BusinessException;
import com.nun.aitestcase.entity.User;
import com.nun.aitestcase.mapper.UserMapper;
import com.nun.aitestcase.util.JwtUtil;
import com.nun.aitestcase.vo.LoginResponse;
import com.nun.aitestcase.vo.UserVO;
import jakarta.annotation.PostConstruct;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AuthService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserMapper userMapper, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @PostConstruct
    public void migrateAdminPassword() {
        User admin = userMapper.selectByUsername("admin");
        if (admin != null && !admin.getPassword().startsWith("$2a$") && !admin.getPassword().startsWith("$2b$")) {
            admin.setPassword(passwordEncoder.encode(admin.getPassword()));
            userMapper.updateById(admin);
        }
    }

    public LoginResponse login(String username, String password) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            throw new BusinessException("Username and password are required");
        }
        User user = userMapper.selectByUsername(username.trim());
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException("Username or password is incorrect");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        return new LoginResponse(token, toUserVO(user));
    }

    public LoginResponse register(String username, String password, String realName) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password) || !StringUtils.hasText(realName)) {
            throw new BusinessException("Username, password and real name are required");
        }
        String trimmedUsername = username.trim();
        if (userMapper.selectByUsername(trimmedUsername) != null) {
            throw new BusinessException(409, "Username already exists");
        }
        User user = new User();
        user.setUsername(trimmedUsername);
        user.setPassword(passwordEncoder.encode(password));
        user.setRealName(realName.trim());
        user.setRole("MEMBER");
        userMapper.insert(user);
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        return new LoginResponse(token, toUserVO(user));
    }

    private UserVO toUserVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setRole(user.getRole());
        return vo;
    }
}
