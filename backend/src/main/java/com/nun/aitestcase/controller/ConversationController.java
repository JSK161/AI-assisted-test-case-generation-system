package com.nun.aitestcase.controller;

import com.nun.aitestcase.common.ApiResponse;
import com.nun.aitestcase.service.ConversationService;
import com.nun.aitestcase.vo.ConversationDetailVO;
import com.nun.aitestcase.vo.ConversationListItemVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @GetMapping
    public ApiResponse<List<ConversationListItemVO>> list(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResponse.success(conversationService.listByUser(userId));
    }

    @GetMapping("/{id}")
    public ApiResponse<ConversationDetailVO> detail(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResponse.success(conversationService.getDetail(id, userId));
    }

    @PostMapping
    public ApiResponse<Map<String, Long>> create(@RequestBody Map<String, String> body, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String title = body.getOrDefault("title", "新对话");
        String requirement = body.get("requirement");
        Long id = conversationService.create(userId, title, requirement);
        return ApiResponse.success(Map.of("id", id));
    }

    @PostMapping("/{id}/messages")
    public ApiResponse<Void> addMessage(@PathVariable Long id, @RequestBody Map<String, String> body, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        conversationService.addMessage(id, userId, body.get("sender"), body.get("content"));
        return ApiResponse.success();
    }

    @PutMapping("/{id}/answers")
    public ApiResponse<Void> updateAnswers(@PathVariable Long id, @RequestBody Object answers, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        conversationService.updateAnswers(id, userId, answers);
        return ApiResponse.success();
    }

    @PutMapping("/{id}/plan")
    public ApiResponse<Void> updatePlan(@PathVariable Long id, @RequestBody Object plan, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        conversationService.updatePlan(id, userId, plan);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        conversationService.delete(id, userId);
        return ApiResponse.success();
    }
}
