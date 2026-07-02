package com.nun.aitestcase.controller;

import com.nun.aitestcase.common.ApiResponse;
import com.nun.aitestcase.dto.UpdateEmailRequest;
import com.nun.aitestcase.dto.UpdatePasswordRequest;
import com.nun.aitestcase.service.AuthService;
import com.nun.aitestcase.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final AuthService authService;

    public UserController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/profile")
    public ApiResponse<UserVO> profile(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResponse.success(authService.getUserProfile(userId));
    }

    @PutMapping("/email")
    public ApiResponse<Void> updateEmail(@Valid @RequestBody UpdateEmailRequest body, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        authService.updateEmail(userId, body.getEmail());
        return ApiResponse.success();
    }

    @PutMapping("/password")
    public ApiResponse<Void> updatePassword(@Valid @RequestBody UpdatePasswordRequest body, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        authService.updatePassword(userId, body.getCurrentPassword(), body.getNewPassword());
        return ApiResponse.success();
    }

    @GetMapping("/api-key")
    public ApiResponse<Map<String, String>> getApiKey(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String key = authService.getUserApiKey(userId);
        return ApiResponse.success(Map.of("apiKey", key != null ? key : ""));
    }

    @PutMapping("/api-key")
    public ApiResponse<Void> updateApiKey(@RequestBody Map<String, String> body, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        authService.updateApiKey(userId, body.get("apiKey"));
        return ApiResponse.success();
    }
}
