package com.nun.aitestcase.service.ai;

import com.nun.aitestcase.config.DeepSeekProperties;
import com.nun.aitestcase.dto.QuestionGenerateRequest;
import com.nun.aitestcase.vo.GeneratedQuestionOptionVO;
import com.nun.aitestcase.vo.GeneratedQuestionResponseVO;
import com.nun.aitestcase.vo.GeneratedQuestionVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestionGenerationService {

    private final DeepSeekClient deepSeekClient;
    private final DeepSeekProperties properties;
    private final QuestionPromptBuilder promptBuilder;
    private final GeneratedQuestionParser questionParser;
    private final UrlContentFetcher urlContentFetcher;

    public QuestionGenerationService(
            DeepSeekClient deepSeekClient,
            DeepSeekProperties properties,
            QuestionPromptBuilder promptBuilder,
            GeneratedQuestionParser questionParser,
            UrlContentFetcher urlContentFetcher
    ) {
        this.deepSeekClient = deepSeekClient;
        this.properties = properties;
        this.promptBuilder = promptBuilder;
        this.questionParser = questionParser;
        this.urlContentFetcher = urlContentFetcher;
    }

    public GeneratedQuestionResponseVO generate(QuestionGenerateRequest request) {
        if (properties.isMockEnabled() && !DeepSeekClient.hasUserApiKey()) {
            return createPreviewQuestions(request.getRequirement());
        }

        String referenceContent = urlContentFetcher.fetch(request.getReferenceUrl());
        String prompt = promptBuilder.build(request.getRequirement(), request.getFileContent(), referenceContent);
        String content = deepSeekClient.generateTestCases(prompt);
        return questionParser.parse(content, properties.getModel());
    }

    private GeneratedQuestionResponseVO createPreviewQuestions(String requirement) {
        GeneratedQuestionResponseVO response = new GeneratedQuestionResponseVO();
        response.setQuestions(new GeneratedQuestionParser(new com.fasterxml.jackson.databind.ObjectMapper())
                .parse("""
                        {
                          "questions": [
                            {
                              "id": "business_flow",
                              "title": "这个模块最核心的业务流程是什么？",
                              "type": "multiple",
                              "allowCustom": true,
                              "options": [
                                {"label": "正常提交成功", "value": "正常提交成功", "description": "覆盖主链路"},
                                {"label": "数据校验失败", "value": "数据校验失败", "description": "覆盖异常输入"},
                                {"label": "状态回滚或重试", "value": "状态回滚或重试", "description": "覆盖失败恢复"}
                              ]
                            },
                            {
                              "id": "risk_focus",
                              "title": "你更关注哪些测试风险？",
                              "type": "multiple",
                              "allowCustom": true,
                              "options": [
                                {"label": "权限与越权", "value": "权限与越权", "description": "验证不同角色边界"},
                                {"label": "并发与重复提交", "value": "并发与重复提交", "description": "验证重复请求处理"},
                                {"label": "弱网与超时", "value": "弱网与超时", "description": "验证异常环境"}
                              ]
                            }
                          ]
                        }
                        """, "本地预览").getQuestions());
        response.setUsedModel("本地预览");
        return response;
    }
}
