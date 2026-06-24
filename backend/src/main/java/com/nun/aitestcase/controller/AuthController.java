package com.nun.aitestcase.controller;

import com.nun.aitestcase.common.ApiResponse;
import com.nun.aitestcase.dto.LoginRequest;
import com.nun.aitestcase.dto.RegisterRequest;
import com.nun.aitestcase.dto.UpdateEmailRequest;
import com.nun.aitestcase.dto.UpdatePasswordRequest;
import com.nun.aitestcase.service.AuthService;
import com.nun.aitestcase.vo.LoginResponse;
import com.nun.aitestcase.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request.getUsername(), request.getPassword()));
    }

    @PostMapping("/register")
    public ApiResponse<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success(
                authService.register(request.getUsername(), request.getPassword(), request.getRealName(), request.getEmail()));
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
}
