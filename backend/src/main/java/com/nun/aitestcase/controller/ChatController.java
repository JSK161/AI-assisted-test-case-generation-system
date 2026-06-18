package com.nun.aitestcase.controller;

import com.nun.aitestcase.common.ApiResponse;
import com.nun.aitestcase.dto.ChatGenerateRequest;
import com.nun.aitestcase.dto.QuestionGenerateRequest;
import com.nun.aitestcase.service.ai.ChatGenerationService;
import com.nun.aitestcase.service.ai.QuestionGenerationService;
import com.nun.aitestcase.vo.GeneratedQuestionResponseVO;
import com.nun.aitestcase.vo.GeneratedPlanVO;
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

    public ChatController(ChatGenerationService chatGenerationService, QuestionGenerationService questionGenerationService) {
        this.chatGenerationService = chatGenerationService;
        this.questionGenerationService = questionGenerationService;
    }

    @PostMapping("/questions")
    public ApiResponse<GeneratedQuestionResponseVO> questions(@Valid @RequestBody QuestionGenerateRequest request) {
        return ApiResponse.success(questionGenerationService.generate(request));
    }

    @PostMapping("/generate")
    public ApiResponse<GeneratedPlanVO> generate(@Valid @RequestBody ChatGenerateRequest request) {
        return ApiResponse.success(chatGenerationService.generate(request));
    }
}
