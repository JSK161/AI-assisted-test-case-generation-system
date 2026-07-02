package com.nun.aitestcase.service.ai;

import com.nun.aitestcase.config.DeepSeekProperties;
import com.nun.aitestcase.dto.ChatAnswerDTO;
import com.nun.aitestcase.dto.ChatGenerateRequest;
import com.nun.aitestcase.vo.GeneratedPlanVO;
import com.nun.aitestcase.vo.TestCasePlanItemVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class ChatGenerationService {

    private final DeepSeekClient deepSeekClient;
    private final DeepSeekProperties properties;
    private final ChatPromptBuilder promptBuilder;
    private final GeneratedPlanParser planParser;
    private final UrlContentFetcher urlContentFetcher;

    public ChatGenerationService(
            DeepSeekClient deepSeekClient,
            DeepSeekProperties properties,
            ChatPromptBuilder promptBuilder,
            GeneratedPlanParser planParser,
            UrlContentFetcher urlContentFetcher
    ) {
        this.deepSeekClient = deepSeekClient;
        this.properties = properties;
        this.promptBuilder = promptBuilder;
        this.planParser = planParser;
        this.urlContentFetcher = urlContentFetcher;
    }

    public GeneratedPlanVO generate(ChatGenerateRequest request) {
        if (properties.isMockEnabled() && !DeepSeekClient.hasUserApiKey()) {
            return createPreviewPlan(request, "本地预览");
        }

        String referenceContent = urlContentFetcher.fetch(request.getReferenceUrl());
        String prompt = promptBuilder.build(request.getRequirement(), request.getAnswers(), request.getFileContent(), referenceContent);
        String content = deepSeekClient.generateTestCases(prompt);
        return planParser.parse(content, request.getRequirement(), properties.getModel());
    }

    private GeneratedPlanVO createPreviewPlan(ChatGenerateRequest request, String usedModel) {
        String moduleName = extractModuleName(request.getRequirement());
        String answerSummary = promptBuilder.summarizeAnswers(request.getAnswers());

        GeneratedPlanVO plan = new GeneratedPlanVO();
        plan.setTitle(moduleName + "测试用例方案");
        plan.setSummary(request.getRequirement() + "。系统已根据补充信息生成预览方案，接入 DeepSeek API Key 后可生成更完整结果。");
        plan.setScope(List.of(
                "被测模块：" + moduleName,
                "测试类型：功能测试、安全测试、兼容性测试、边界值测试、异常流程测试",
                "补充信息：" + answerSummary
        ));
        plan.setRisks(List.of(
                "账号、验证码、Session、Cookie 等状态类逻辑需要重点验证。",
                "错误提示需要清晰且不能暴露敏感信息。",
                "弱网、刷新、返回、多端登录等操作容易出现状态不一致问题。"
        ));
        plan.setTestCases(List.of(
                testCase("TC-001", moduleName + "正常流程验证", "P0", "正常场景",
                        "测试环境可访问，测试账号或测试数据已准备完成。",
                        List.of("打开被测页面或入口", "输入合法数据", "提交操作", "观察页面跳转、状态变化和接口返回"),
                        "系统处理成功，页面、接口返回和业务状态均符合需求预期。"),
                testCase("TC-002", moduleName + "必填项为空校验", "P0", "异常场景",
                        "用户进入被测功能页面。",
                        List.of("清空一个或多个必填项", "点击提交按钮", "观察表单校验提示"),
                        "系统阻止提交，并在对应字段附近提示必填或格式错误。"),
                testCase("TC-003", moduleName + "边界值与特殊字符校验", "P1", "边界场景",
                        "已明确字段长度、格式和允许字符范围。",
                        List.of("输入最小长度、最大长度和超长数据", "输入空格、中文、Unicode、特殊符号", "分别提交并观察结果"),
                        "系统按照字段规则处理，非法数据被拦截，合法边界值可以正常通过。"),
                testCase("TC-004", moduleName + "安全与会话状态验证", "P1", "安全场景",
                        "准备正常账号、异常账号及多个浏览器标签页。",
                        List.of("尝试重复提交、刷新页面、回退浏览器", "模拟 Session 过期或 Cookie 失效", "观察权限与状态是否正确"),
                        "系统不会出现越权、重复提交、状态错乱或敏感信息泄露。")
        ));
        plan.setUsedModel(usedModel);
        return plan;
    }

    private TestCasePlanItemVO testCase(
            String id,
            String title,
            String priority,
            String category,
            String precondition,
            List<String> steps,
            String expectedResult
    ) {
        TestCasePlanItemVO item = new TestCasePlanItemVO();
        item.setId(id);
        item.setTitle(title);
        item.setPriority(priority);
        item.setCategory(category);
        item.setPrecondition(precondition);
        item.setSteps(steps);
        item.setExpectedResult(expectedResult);
        return item;
    }

    private String extractModuleName(String requirement) {
        if (!StringUtils.hasText(requirement)) {
            return "目标模块";
        }
        if (requirement.contains("登录")) {
            return "登录模块";
        }
        if (requirement.contains("接口")) {
            return "接口模块";
        }
        String normalized = requirement.replaceAll("[，。,.]", " ").trim();
        return normalized.length() > 16 ? normalized.substring(0, 16) : normalized;
    }
}
