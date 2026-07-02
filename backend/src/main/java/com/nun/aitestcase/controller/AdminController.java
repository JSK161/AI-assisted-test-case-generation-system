package com.nun.aitestcase.controller;

import com.nun.aitestcase.common.ApiResponse;
import com.nun.aitestcase.service.AdminService;
import com.nun.aitestcase.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    public ApiResponse<List<UserVO>> listUsers(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResponse.success(adminService.listUsers(userId));
    }

    @PutMapping("/users/{id}/role")
    public ApiResponse<Void> updateRole(@PathVariable Long id, @RequestBody Map<String, String> body,
                                         HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        adminService.updateRole(userId, id, body.get("role"));
        return ApiResponse.success();
    }

    @DeleteMapping("/users/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        adminService.deleteUser(userId, id);
        return ApiResponse.success();
    }
}
