package com.nun.aitestcase.service;

import com.nun.aitestcase.common.BusinessException;
import com.nun.aitestcase.entity.User;
import com.nun.aitestcase.mapper.UserMapper;
import com.nun.aitestcase.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Test
    void loginReturnsTokenWhenPasswordMatches() {
        UserMapper userMapper = mock(UserMapper.class);
        JwtUtil jwtUtil = mock(JwtUtil.class);
        String hashedPassword = new BCryptPasswordEncoder().encode("123456");
        User user = new User();
        user.setId(1L);
        user.setUsername("admin");
        user.setPassword(hashedPassword);
        user.setRealName("Admin");
        user.setRole("ADMIN");
        when(userMapper.selectByUsername("admin")).thenReturn(user);
        when(jwtUtil.generateToken(1L, "admin", "ADMIN")).thenReturn("test-token");

        AuthService authService = new AuthService(userMapper, jwtUtil);
        var result = authService.login("admin", "123456");

        assertEquals("test-token", result.getToken());
        assertEquals("admin", result.getUser().getUsername());
        assertEquals("ADMIN", result.getUser().getRole());
    }

    @Test
    void loginRejectsWrongPassword() {
        UserMapper userMapper = mock(UserMapper.class);
        JwtUtil jwtUtil = mock(JwtUtil.class);
        String hashedPassword = new BCryptPasswordEncoder().encode("123456");
        User user = new User();
        user.setUsername("admin");
        user.setPassword(hashedPassword);
        when(userMapper.selectByUsername("admin")).thenReturn(user);

        AuthService authService = new AuthService(userMapper, jwtUtil);
        assertThrows(BusinessException.class, () -> authService.login("admin", "bad-password"));
    }
}
