package com.nun.aitestcase.controller;

import com.nun.aitestcase.common.ApiResponse;
import com.nun.aitestcase.dto.ChatGenerateRequest;
import com.nun.aitestcase.dto.QuestionGenerateRequest;
import com.nun.aitestcase.service.ConversationService;
import com.nun.aitestcase.service.ai.ChatGenerationService;
import com.nun.aitestcase.service.ai.QuestionGenerationService;
import com.nun.aitestcase.vo.GeneratedPlanVO;
import com.nun.aitestcase.vo.GeneratedQuestionResponseVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatGenerationService chatGenerationService;
    private final QuestionGenerationService questionGenerationService;
    private final ConversationService conversationService;

    public ChatController(ChatGenerationService chatGenerationService,
                          QuestionGenerationService questionGenerationService,
                          ConversationService conversationService) {
        this.chatGenerationService = chatGenerationService;
        this.questionGenerationService = questionGenerationService;
        this.conversationService = conversationService;
    }

    @PostMapping("/questions")
    public ApiResponse<GeneratedQuestionResponseVO> questions(
            @Valid @RequestBody QuestionGenerateRequest request,
            HttpServletRequest httpRequest) {
        GeneratedQuestionResponseVO result = questionGenerationService.generate(request);
        saveChatContext(httpRequest, request.getConversationId(), request.getRequirement(), result, null);
        return ApiResponse.success(result);
    }

    @PostMapping("/generate")
    public ApiResponse<GeneratedPlanVO> generate(
            @Valid @RequestBody ChatGenerateRequest request,
            HttpServletRequest httpRequest) {
        GeneratedPlanVO result = chatGenerationService.generate(request);
        saveChatContext(httpRequest, request.getConversationId(), request.getRequirement(), null, request.getAnswers());
        return ApiResponse.success(result);
    }

    private void saveChatContext(HttpServletRequest request, Long conversationId,
                                  String requirement, Object result, Object answers) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return;
        try {
            if (conversationId == null) {
                conversationId = conversationService.create(userId,
                        requirement.length() > 50 ? requirement.substring(0, 50) + "..." : requirement,
                        requirement);
                conversationService.addMessage(conversationId, userId, "user", requirement);
            }
            if (result instanceof GeneratedQuestionResponseVO) {
                GeneratedQuestionResponseVO qr = (GeneratedQuestionResponseVO) result;
                String msg = "已根据您的需求生成" + (qr.getQuestions() != null ? qr.getQuestions().size() : 0) + "个补充问题";
                conversationService.addMessage(conversationId, userId, "assistant", msg);
                if (answers != null) {
                    conversationService.updateAnswers(conversationId, userId, answers);
                }
            } else if (result instanceof GeneratedPlanVO) {
                conversationService.addMessage(conversationId, userId, "assistant", "测试方案已生成");
                conversationService.updatePlan(conversationId, userId, result);
            }
        } catch (Exception ignored) {
            // Conversation saving is best-effort
        }
    }
}
