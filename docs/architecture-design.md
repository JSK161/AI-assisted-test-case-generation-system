# AI辅助测试用例生成系统架构设计说明书

## 1. 文档概述

本文档描述 AI辅助测试用例生成系统的总体架构、技术选型、模块划分、接口设计、数据设计和核心业务流程。当前版本的系统主线是 AI 对话式测试用例生成，即用户输入待测试模块后，系统通过 DeepSeek 动态反问补齐信息，再生成结构化测试用例方案。

## 2. 总体架构

### 2.1 架构风格

系统采用前后端分离架构：

1. 前端负责用户界面、交互流程、动态问题展示和生成结果展示。
2. 后端负责 REST API、DeepSeek 调用、Prompt 构造、JSON 解析、异常处理和数据访问。
3. 数据库负责基础用户、项目、需求、测试用例和 AI 生成记录持久化。
4. DeepSeek API 作为外部 AI 服务，由后端统一调用。

### 2.2 技术栈

前端技术：

1. Vue 3。
2. TypeScript。
3. Vite。
4. Element Plus。
5. lucide 图标。
6. Axios。
7. Vitest。

后端技术：

1. Java 17。
2. Spring Boot 3。
3. Spring Validation。
4. MyBatis-Plus。
5. MySQL Driver。
6. Jackson。
7. Springdoc OpenAPI。

数据库：

1. MySQL 8。

AI 服务：

1. DeepSeek API。
2. 默认模型 `deepseek-chat`。

### 2.3 架构关系

```text
Vue + TypeScript 前端
  |
  | Axios / REST API
  v
Spring Boot 后端
  |                 |
  | MyBatis-Plus    | RestClient
  v                 v
MySQL 数据库       DeepSeek API
```

### 2.4 部署端口

```text
前端开发服务：http://localhost:5173
后端服务：http://localhost:8080
MySQL 数据库：localhost:3306
```

## 3. 前端架构设计

### 3.1 前端目录结构

```text
frontend/
  src/
    api/
      chat.ts
    router/
      index.ts
    styles/
      main.css
    types/
      chat.ts
    utils/
      chatFlow.ts
      request.ts
      statistics.ts
      status.ts
    views/
      AiChatView.vue
    App.vue
    main.ts
```

### 3.2 前端模块职责

| 模块 | 说明 |
| --- | --- |
| `AiChatView.vue` | AI 对话式主页面，负责需求输入、动态问题展示、用户回答收集和结果展示 |
| `api/chat.ts` | 封装 `/api/chat/questions` 和 `/api/chat/generate` 接口请求 |
| `types/chat.ts` | 定义动态问题、用户回答、测试方案、测试用例等 TypeScript 类型 |
| `utils/chatFlow.ts` | 处理动态问题顺序、确认步骤追加、回答摘要、降级预览结果 |
| `utils/request.ts` | 封装 Axios 请求、统一错误处理和静默降级配置 |
| `styles/main.css` | 定义 AI 对话页面整体视觉样式和响应式布局 |
| `router/index.ts` | 路由配置，默认进入 AI 对话页面 |

### 3.3 前端页面结构

前端页面分为三个阶段：

1. 首页输入阶段：用户输入测试模块或功能，可填写参考 URL。
2. 动态问答阶段：展示 DeepSeek 生成的问题和选项，收集用户回答。
3. 结果展示阶段：展示测试方案标题、范围表格、测试用例列表和风险提示。

### 3.4 前端状态设计

核心状态包括：

| 状态 | 说明 |
| --- | --- |
| `stage` | 当前页面阶段，包含 `home`、`questions`、`result` |
| `requirementInput` | 用户输入的测试需求 |
| `referenceUrl` | 可选参考 URL |
| `questions` | DeepSeek 返回的动态反问问题 |
| `answers` | 用户对动态问题的回答 |
| `generatedPlan` | DeepSeek 返回的测试用例方案 |
| `usedModel` | 生成方案使用的模型名称 |
| `isPreparingQuestions` | 是否正在生成动态反问问题 |

## 4. 后端架构设计

### 4.1 后端包结构

```text
backend/src/main/java/com/nun/aitestcase/
  common/
  config/
  controller/
  dto/
  entity/
  exception/
  mapper/
  service/
  service/ai/
  vo/
```

### 4.2 后端模块职责

| 包 | 职责 |
| --- | --- |
| `controller` | 提供 REST API 接口 |
| `service` | 实现基础业务逻辑 |
| `service/ai` | DeepSeek 调用、Prompt 构造、AI 返回解析、URL 内容读取 |
| `dto` | 接收前端请求参数 |
| `vo` | 返回前端展示数据 |
| `entity` | 数据库实体对象 |
| `mapper` | MyBatis-Plus 数据访问 |
| `config` | 跨域、DeepSeek 参数配置 |
| `exception` | 统一异常处理 |
| `common` | 统一响应、业务异常、枚举 |

### 4.3 AI 服务模块设计

AI 服务模块位于 `service/ai`，包含以下核心类：

| 类 | 说明 |
| --- | --- |
| `DeepSeekClient` | 负责调用 DeepSeek Chat Completions API |
| `QuestionPromptBuilder` | 构造动态反问 Prompt |
| `QuestionGenerationService` | 组织动态反问生成流程 |
| `GeneratedQuestionParser` | 解析 DeepSeek 返回的动态问题 JSON |
| `ChatPromptBuilder` | 构造测试用例生成 Prompt |
| `ChatGenerationService` | 组织测试方案生成流程 |
| `GeneratedPlanParser` | 解析 DeepSeek 返回的测试方案 JSON |
| `UrlContentFetcher` | 读取和清洗公开 URL 页面内容 |
| `AiResponseParser` | 兼容旧版 AI 生成测试用例接口解析 |

### 4.4 后端统一响应

系统使用 `ApiResponse<T>` 作为统一响应结构：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

异常由 `GlobalExceptionHandler` 统一捕获并返回错误响应。

## 5. 核心业务流程

### 5.1 AI 动态反问流程

```text
用户输入测试模块
  -> 前端调用 POST /api/chat/questions
  -> 后端读取可选参考 URL
  -> QuestionPromptBuilder 构造反问 Prompt
  -> DeepSeekClient 调用 DeepSeek API
  -> GeneratedQuestionParser 解析 questions JSON
  -> 后端追加 confirm 确认步骤
  -> 前端展示动态问题
```

示例：用户输入“订单退款模块”时，系统可能生成如下问题：

1. 退款功能涉及哪些平台或端？
2. 退款审核流程是怎样的？
3. 原路退回支持哪些支付渠道？
4. 退款失败时系统应如何处理？
5. 订单状态回滚需要覆盖哪些场景？

### 5.2 测试用例生成流程

```text
用户完成动态问答
  -> 前端调用 POST /api/chat/generate
  -> 后端读取原始需求、用户回答、参考 URL
  -> ChatPromptBuilder 构造测试用例生成 Prompt
  -> DeepSeekClient 调用 DeepSeek API
  -> GeneratedPlanParser 解析测试方案 JSON
  -> 前端展示测试范围、风险提示和测试用例列表
```

### 5.3 本地预览降级流程

当 DeepSeek API Key 未配置、网络异常或模型返回格式异常时，系统进入降级流程：

```text
DeepSeek 调用失败
  -> 后端或前端返回本地预览数据
  -> 前端继续展示可演示内容
  -> 页面不阻断用户流程
```

该机制用于保证实训演示过程稳定。

### 5.4 复制与导出流程

```text
用户查看生成方案
  -> 点击复制方案
  -> 前端转换为 Markdown 文本
  -> 写入剪贴板

用户点击导出 Markdown
  -> 前端生成 Blob
  -> 浏览器下载 .md 文件
```

## 6. DeepSeek API 设计

### 6.1 调用原则

1. DeepSeek API 只能由后端调用。
2. 前端不保存、不展示、不传输 DeepSeek API Key。
3. API Key 从环境变量 `DEEPSEEK_API_KEY` 读取。
4. 是否启用本地 mock 由 `DEEPSEEK_MOCK_ENABLED` 控制。

### 6.2 配置方式

```yaml
deepseek:
  api-key: ${DEEPSEEK_API_KEY:}
  base-url: ${DEEPSEEK_BASE_URL:https://api.deepseek.com}
  model: ${DEEPSEEK_MODEL:deepseek-chat}
  mock-enabled: ${DEEPSEEK_MOCK_ENABLED:true}
```

### 6.3 动态反问 Prompt

动态反问 Prompt 要求 DeepSeek：

1. 扮演资深软件测试工程师。
2. 不直接生成测试用例，而是先反问用户。
3. 根据不同模块生成不同问题。
4. 返回严格 JSON。
5. 生成 3 到 5 个问题。
6. 每个问题提供 2 到 5 个具体选项。

### 6.4 测试方案 Prompt

测试方案 Prompt 要求 DeepSeek：

1. 根据原始需求和用户回答生成方案。
2. 返回严格 JSON。
3. 输出方案标题、摘要、范围、风险和测试用例列表。
4. 至少覆盖正常流程、异常输入、边界值、安全、兼容性和体验场景。
5. 测试用例必须包含步骤和预期结果。

### 6.5 AI 返回解析策略

模型返回内容由后端 Parser 处理：

1. 提取首个 `{` 到最后一个 `}` 之间的 JSON。
2. 使用 Jackson 解析为对象。
3. 对缺失字段设置默认值。
4. 限制问题和选项数量，避免异常超长内容。
5. 解析失败时抛出业务异常，由前端降级展示。

## 7. 接口设计

### 7.1 生成动态反问问题

```text
POST /api/chat/questions
```

请求示例：

```json
{
  "requirement": "我想测试订单退款模块，需要考虑退款审核和原路退回",
  "referenceUrl": ""
}
```

响应示例：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "usedModel": "deepseek-chat",
    "questions": [
      {
        "id": "refund_scenario",
        "title": "退款场景主要涉及哪些业务流程？",
        "type": "multiple",
        "allowCustom": true,
        "options": [
          {
            "label": "仅退款",
            "value": "仅退款",
            "description": "适用于未发货或无需退货场景"
          }
        ]
      }
    ]
  }
}
```

### 7.2 生成测试用例方案

```text
POST /api/chat/generate
```

请求示例：

```json
{
  "requirement": "我想测试订单退款模块",
  "answers": [
    {
      "questionId": "refund_scenario",
      "values": ["仅退款", "退货退款"],
      "customText": "需要覆盖退款失败重试"
    }
  ],
  "referenceUrl": ""
}
```

响应示例：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "title": "订单退款模块测试方案",
    "summary": "覆盖退款申请、审核、原路退回、失败处理等场景。",
    "scope": ["被测模块：订单退款模块"],
    "risks": ["退款失败后订单状态可能不一致"],
    "testCases": [
      {
        "id": "TC-001",
        "title": "用户提交仅退款申请",
        "priority": "P0",
        "category": "正常场景",
        "precondition": "订单已支付且满足退款条件",
        "steps": ["进入订单详情", "点击申请退款", "提交退款原因"],
        "expectedResult": "退款申请提交成功，订单状态更新为退款处理中"
      }
    ],
    "usedModel": "deepseek-chat"
  }
}
```

### 7.3 基础支撑接口

系统保留基础接口：

```text
POST /api/auth/login
POST /api/projects
GET  /api/projects
GET  /api/projects/{id}
POST /api/requirements
GET  /api/requirements/project/{projectId}
POST /api/ai/generate-test-cases
GET  /api/test-cases/project/{projectId}
PUT  /api/test-cases/{id}
PUT  /api/test-cases/{id}/adoption-status
GET  /api/statistics/project/{projectId}
```

## 8. 数据库设计

### 8.1 用户表 `sys_user`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | BIGINT | 主键 |
| username | VARCHAR(50) | 用户名 |
| password | VARCHAR(100) | 密码 |
| real_name | VARCHAR(50) | 真实姓名 |
| role | VARCHAR(20) | 角色 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

### 8.2 项目表 `project`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | BIGINT | 主键 |
| name | VARCHAR(100) | 项目名称 |
| description | TEXT | 项目描述 |
| creator_id | BIGINT | 创建人 ID |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

### 8.3 需求表 `requirement`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | BIGINT | 主键 |
| project_id | BIGINT | 项目 ID |
| title | VARCHAR(100) | 需求标题 |
| content | TEXT | 需求描述 |
| created_by | BIGINT | 创建人 ID |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

### 8.4 测试用例表 `test_case`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | BIGINT | 主键 |
| project_id | BIGINT | 项目 ID |
| requirement_id | BIGINT | 需求 ID |
| title | VARCHAR(200) | 用例标题 |
| type | VARCHAR(50) | 用例类型 |
| precondition | TEXT | 前置条件 |
| steps | TEXT | 测试步骤 JSON 字符串 |
| test_data | TEXT | 测试数据 |
| expected_result | TEXT | 预期结果 |
| priority | VARCHAR(20) | 优先级 |
| adoption_status | VARCHAR(20) | 采纳状态 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

### 8.5 AI 生成记录表 `ai_generation_record`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | BIGINT | 主键 |
| project_id | BIGINT | 项目 ID |
| requirement_id | BIGINT | 需求 ID |
| model_name | VARCHAR(50) | 模型名称 |
| prompt | TEXT | 提示词 |
| response_content | LONGTEXT | AI 原始返回内容 |
| status | VARCHAR(20) | 调用状态 |
| error_message | TEXT | 错误信息 |
| created_at | DATETIME | 创建时间 |

## 9. 安全设计

1. DeepSeek API Key 通过环境变量配置。
2. `.gitignore` 排除 `.env`、构建产物、依赖目录和日志文件。
3. 后端统一调用 DeepSeek，前端不接触密钥。
4. 请求参数使用 Validation 校验。
5. 异常信息通过统一响应返回，避免系统直接崩溃。

## 10. 测试设计

### 10.1 前端测试

前端使用 Vitest 测试工具函数：

1. 动态问题顺序推进。
2. 用户回答摘要生成。
3. 本地预览测试方案生成。
4. 状态统计工具函数。

### 10.2 后端测试

后端使用 JUnit 测试：

1. AI 测试用例 JSON 解析。
2. 动态反问 Prompt 构造。
3. 动态反问 JSON 解析。
4. 测试方案 JSON 解析。
5. 登录服务。
6. 统计服务。

## 11. 部署设计

### 11.1 启动顺序

```text
1. 启动 MySQL
2. 导入 database/schema.sql
3. 配置 DEEPSEEK_API_KEY
4. 启动 Spring Boot 后端
5. 启动 Vue 前端
```

### 11.2 后端启动

```bash
cd backend
mvn spring-boot:run
```

### 11.3 前端启动

```bash
cd frontend
npm install
npm run dev
```

### 11.4 DeepSeek 环境变量

```powershell
$env:DEEPSEEK_API_KEY="你的_API_KEY"
$env:DEEPSEEK_MOCK_ENABLED="false"
```

## 12. 架构验收标准

1. 前后端分离，前端通过 REST API 调用后端。
2. DeepSeek API Key 不出现在前端代码和 GitHub 仓库中。
3. 系统能够根据不同模块动态生成不同反问问题。
4. 系统能够根据用户回答生成结构化测试用例方案。
5. 页面可以展示、复制和导出测试方案。
6. 后端分层清晰，AI 调用、Prompt 构造、JSON 解析职责独立。
7. 前端构建、前端测试和后端测试能够通过。
