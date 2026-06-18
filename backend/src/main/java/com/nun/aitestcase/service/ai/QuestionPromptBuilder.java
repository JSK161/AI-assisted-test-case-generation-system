package com.nun.aitestcase.service.ai;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class QuestionPromptBuilder {

    public String build(String requirement, String referenceContent) {
        String referenceText = StringUtils.hasText(referenceContent)
                ? referenceContent
                : "用户没有提供可读取的参考页面内容。";

        return """
                你是一名资深软件测试工程师。用户会告诉你“想测试某个模块/功能/接口”，你的任务不是直接生成测试用例，而是先像测试专家一样反问用户，补齐生成测试用例所需的关键信息。

                输出必须是严格 JSON，不要使用 Markdown 代码块，不要添加 JSON 之外的解释文字。

                JSON 字段要求：
                {
                  "questions": [
                    {
                      "id": "英文或拼音标识，不能重复",
                      "title": "针对当前需求的中文反问问题",
                      "type": "single 或 multiple",
                      "allowCustom": true,
                      "options": [
                        {
                          "label": "中文选项",
                          "value": "中文选项，尽量与 label 一致",
                          "description": "为什么这个选项重要"
                        }
                      ]
                    }
                  ]
                }

                反问规则：
                1. 根据用户需求动态生成 3 到 5 个反问问题，不要输出固定模板问题。
                2. 每个问题都必须和用户提到的模块强相关，例如登录、支付、退款、接口、后台权限等要问不同问题。
                3. 优先询问会影响测试用例差异的内容：业务流程、平台端、角色权限、数据状态、异常分支、安全要求、第三方依赖。
                4. 每个问题提供 2 到 5 个可选答案；选项要具体，避免“其他情况”这种空泛表达。
                5. value 使用中文，便于后续直接拼入测试用例生成 prompt。
                6. 不要生成“是否确认生成”问题，确认步骤由系统界面负责。

                用户需求：
                %s

                参考页面内容：
                %s
                """.formatted(requirement, referenceText);
    }
}
