package com.nun.aitestcase.service;

import com.nun.aitestcase.common.BusinessException;
import com.nun.aitestcase.entity.User;
import com.nun.aitestcase.mapper.UserMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    @Test
    void loginReturnsUserWhenPasswordMatches() {
        UserMapper userMapper = mock(UserMapper.class);
        User user = new User();
        user.setId(1L);
        user.setUsername("admin");
        user.setPassword("123456");
        user.setRealName("Jia Shukai");
        user.setRole("ADMIN");
        when(userMapper.selectByUsername("admin")).thenReturn(user);

        AuthService authService = new AuthService(userMapper);

        User result = authService.login("admin", "123456");

        assertEquals(1L, result.getId());
        assertEquals("admin", result.getUsername());
        assertEquals("ADMIN", result.getRole());
    }

    @Test
    void loginRejectsWrongPassword() {
        UserMapper userMapper = mock(UserMapper.class);
        User user = new User();
        user.setUsername("admin");
        user.setPassword("123456");
        when(userMapper.selectByUsername("admin")).thenReturn(user);

        AuthService authService = new AuthService(userMapper);

        assertThrows(BusinessException.class, () -> authService.login("admin", "bad-password"));
    }
}
