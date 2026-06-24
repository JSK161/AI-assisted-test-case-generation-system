package com.nun.aitestcase.controller;

import com.nun.aitestcase.common.ApiResponse;
import com.nun.aitestcase.dto.UpdateEmailRequest;
import com.nun.aitestcase.dto.UpdatePasswordRequest;
import com.nun.aitestcase.service.AuthService;
import com.nun.aitestcase.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

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
}
