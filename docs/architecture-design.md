# AI辅助测试用例生成系统架构设计说明书

## 1. 文档概述

本文档描述 AI辅助测试用例生成系统基础层的总体架构、技术选型、模块划分、数据设计和核心业务流程。基础层目标是完成一个可运行、可演示、可部署的 Web 系统，实现从需求输入到 AI 生成测试用例再到采纳率统计的完整流程。

## 2. 技术架构

### 2.1 技术栈

系统采用前后端分离架构。

前端技术：

1. Vue 3。
2. Vite。
3. Element Plus。
4. Axios。
5. ECharts。

后端技术：

1. Java。
2. Spring Boot。
3. MyBatis-Plus。
4. MySQL Driver。
5. Knife4j 或 Swagger。

数据库：

1. MySQL 8.x。

AI能力：

1. DeepSeek API。

### 2.2 总体架构

系统由四个主要部分组成：

1. 前端展示层：负责页面展示、表单输入、按钮操作、结果展示和统计图表。
2. 后端业务层：负责接口处理、业务逻辑、AI调用、数据校验和异常处理。
3. 数据持久层：负责项目、需求、测试用例、用户和 AI 调用记录的存储。
4. AI服务层：通过后端调用 DeepSeek API，生成结构化测试用例。

架构流程如下：

```text
Vue 前端
  |
  | Axios 请求
  v
Spring Boot 后端
  |
  | MyBatis-Plus
  v
MySQL 数据库

Spring Boot 后端
  |
  | HTTP 请求
  v
DeepSeek API
```

## 3. 系统模块设计

### 3.1 前端模块

前端建议划分为以下模块：

```text
src/
  api/
    auth.js
    project.js
    requirement.js
    testCase.js
    statistics.js
  views/
    LoginView.vue
    ProjectListView.vue
    ProjectDetailView.vue
    RequirementInputView.vue
    TestCaseListView.vue
    StatisticsView.vue
  router/
    index.js
  stores/
    user.js
  utils/
    request.js
```

模块职责：

1. `api`：封装后端接口请求。
2. `views`：实现页面。
3. `router`：管理页面路由。
4. `stores`：保存登录用户信息。
5. `utils/request.js`：封装 Axios 请求和异常处理。

### 3.2 后端模块

后端建议划分为以下包：

```text
com.example.aitestcase
  controller
  service
  service.impl
  mapper
  entity
  dto
  vo
  config
  exception
  common
```

模块职责：

1. `controller`：提供 REST API。
2. `service`：编写业务逻辑。
3. `mapper`：操作 MySQL 数据库。
4. `entity`：对应数据库表。
5. `dto`：接收前端请求参数。
6. `vo`：返回前端展示数据。
7. `config`：配置跨域、Swagger、DeepSeek 参数。
8. `exception`：统一异常处理。
9. `common`：统一返回结构、枚举和工具类。

## 4. 核心业务流程

### 4.1 登录流程

```text
用户输入账号密码
  -> 前端调用 /api/auth/login
  -> 后端查询用户表
  -> 校验账号密码
  -> 返回用户信息
  -> 前端保存登录状态
  -> 跳转项目列表页
```

基础层可以先使用简单登录机制，不强制引入 JWT。若后续需要权限管理，可升级为 JWT 登录。

### 4.2 项目创建流程

```text
用户填写项目名称和描述
  -> 前端提交项目数据
  -> 后端校验项目名称不能为空
  -> 保存项目数据到 MySQL
  -> 返回创建结果
  -> 前端刷新项目列表
```

### 4.3 需求录入流程

```text
用户进入项目详情页
  -> 填写需求标题和需求描述
  -> 前端提交需求数据
  -> 后端校验需求描述
  -> 保存需求数据
  -> 前端展示需求记录
```

### 4.4 AI生成测试用例流程

```text
用户选择需求并点击生成
  -> 前端调用 /api/ai/generate-test-cases
  -> 后端读取需求内容
  -> 后端构造 Prompt
  -> 后端调用 DeepSeek API
  -> DeepSeek 返回 JSON 文本
  -> 后端解析 JSON
  -> 后端保存测试用例
  -> 后端保存 AI 生成记录
  -> 前端展示生成结果
```

### 4.5 采纳率统计流程

```text
用户进入统计页面
  -> 前端调用 /api/statistics/project/{projectId}
  -> 后端查询项目下测试用例
  -> 统计总数、已采纳、需修改、已拒绝
  -> 计算采纳率
  -> 前端使用数字卡片或图表展示
```

## 5. DeepSeek API 设计

### 5.1 调用位置

DeepSeek API 只能由 Spring Boot 后端调用。前端不直接调用 DeepSeek API，避免 API Key 泄露。

### 5.2 配置方式

后端通过配置读取 API Key：

```yaml
deepseek:
  api-key: ${DEEPSEEK_API_KEY}
  base-url: https://api.deepseek.com
  model: deepseek-chat
```

`DEEPSEEK_API_KEY` 应配置在本地环境变量中，不提交到 GitHub。

### 5.3 Prompt 设计

基础层 Prompt 应明确要求 AI 返回 JSON，避免自由文本难以解析。

示例：

```text
你是一名软件测试工程师。请根据以下需求生成测试用例。
要求：
1. 生成正常场景、异常场景和边界场景。
2. 返回 JSON，不要返回 Markdown。
3. JSON 字段包括 title、type、precondition、steps、testData、expectedResult、priority。

需求内容：
{requirementContent}
```

### 5.4 AI返回格式

```json
{
  "testCases": [
    {
      "title": "登录成功测试",
      "type": "正常场景",
      "precondition": "用户已注册账号",
      "steps": [
        "进入登录页面",
        "输入正确手机号",
        "输入正确密码",
        "点击登录按钮"
      ],
      "testData": "手机号：13800000000，密码：123456",
      "expectedResult": "登录成功并跳转首页",
      "priority": "高"
    }
  ]
}
```

### 5.5 异常处理

AI调用可能出现以下异常：

1. API Key 未配置。
2. 网络请求失败。
3. DeepSeek API 返回错误。
4. AI返回内容不是合法 JSON。
5. JSON 字段缺失。

处理方式：

1. 后端记录错误信息。
2. AI生成记录标记为失败。
3. 前端展示友好的错误提示。
4. 不保存不完整的测试用例数据。

## 6. 数据库设计

### 6.1 用户表 sys_user

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | BIGINT | 主键 |
| username | VARCHAR(50) | 用户名 |
| password | VARCHAR(100) | 密码 |
| real_name | VARCHAR(50) | 真实姓名 |
| role | VARCHAR(20) | 角色 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

### 6.2 项目表 project

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | BIGINT | 主键 |
| name | VARCHAR(100) | 项目名称 |
| description | TEXT | 项目描述 |
| creator_id | BIGINT | 创建人ID |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

### 6.3 需求表 requirement

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | BIGINT | 主键 |
| project_id | BIGINT | 项目ID |
| title | VARCHAR(100) | 需求标题 |
| content | TEXT | 需求描述 |
| created_by | BIGINT | 创建人ID |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

### 6.4 测试用例表 test_case

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | BIGINT | 主键 |
| project_id | BIGINT | 项目ID |
| requirement_id | BIGINT | 需求ID |
| title | VARCHAR(200) | 用例标题 |
| type | VARCHAR(50) | 用例类型 |
| precondition | TEXT | 前置条件 |
| steps | TEXT | 测试步骤，JSON数组字符串 |
| test_data | TEXT | 测试数据 |
| expected_result | TEXT | 预期结果 |
| priority | VARCHAR(20) | 优先级 |
| adoption_status | VARCHAR(20) | 采纳状态 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

### 6.5 AI生成记录表 ai_generation_record

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | BIGINT | 主键 |
| project_id | BIGINT | 项目ID |
| requirement_id | BIGINT | 需求ID |
| model_name | VARCHAR(50) | 模型名称 |
| prompt | TEXT | 提示词 |
| response_content | LONGTEXT | AI原始返回内容 |
| status | VARCHAR(20) | 调用状态 |
| error_message | TEXT | 错误信息 |
| created_at | DATETIME | 创建时间 |

## 7. 后端接口设计

### 7.1 登录接口

```text
POST /api/auth/login
```

请求参数：

```json
{
  "username": "admin",
  "password": "123456"
}
```

响应数据：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "admin",
    "realName": "贾舒凯",
    "role": "ADMIN"
  }
}
```

### 7.2 AI生成测试用例接口

```text
POST /api/ai/generate-test-cases
```

请求参数：

```json
{
  "projectId": 1,
  "requirementId": 1
}
```

响应数据：

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "title": "登录成功测试",
      "type": "正常场景",
      "priority": "高",
      "adoptionStatus": "未处理"
    }
  ]
}
```

## 8. 前端页面设计

基础层页面包括：

1. 登录页：输入账号和密码。
2. 项目列表页：展示项目列表，支持创建项目。
3. 项目详情页：展示项目信息、需求列表、统计入口。
4. 需求输入页：录入需求标题和描述。
5. 测试用例列表页：展示 AI 生成的测试用例，支持编辑和采纳状态维护。
6. 统计页：展示总用例数、采纳数、需修改数、拒绝数和采纳率。

## 9. 部署架构

基础层采用本地部署方式：

```text
前端：localhost:5173
后端：localhost:8080
数据库：localhost:3306
```

启动顺序：

1. 启动 MySQL。
2. 创建数据库并导入初始化 SQL。
3. 配置 DeepSeek API Key。
4. 启动 Spring Boot 后端。
5. 启动 Vue 前端。

## 10. 架构验收标准

1. 前后端分离，前端通过 REST API 调用后端。
2. 后端不在 Controller 中直接写数据库操作。
3. DeepSeek API Key 不出现在前端代码和 GitHub 仓库中。
4. AI生成结果可以持久化到 MySQL。
5. 核心流程可以完整演示。
