# AI辅助测试用例生成系统

本项目是北方民族大学计算机科学与工程学院软件工程（中外合办）专业实训项目。当前主流程已调整为“AI 对话式测试用例生成”：用户输入要测试的模块或功能，系统先调用 DeepSeek 动态生成反问问题，用户选择回答后，再调用 DeepSeek 生成结构化测试用例方案。

## 技术栈

- 后端：Java 17 + Spring Boot 3 + MyBatis-Plus
- 前端：Vue 3 + Vite + TypeScript + Element Plus + lucide 图标
- 数据库：MySQL 8
- AI 接口：DeepSeek API

## 项目结构

```text
AI-assisted-test-case-generation-system/
  backend/      Spring Boot 后端
  frontend/     Vue + TypeScript 前端
  database/     MySQL 初始化脚本
  docs/         需求、架构、测试计划说明书
```

## 当前基础功能

1. 对话式需求输入：输入要测试的模块、功能或接口。
2. DeepSeek 动态反问：根据登录、支付、退款、接口等不同模块生成不同补充问题。
3. 可选参考链接：后端可读取公开 URL 内容，辅助生成测试方案。
4. AI 生成方案：通过 `/api/chat/questions` 生成反问问题，通过 `/api/chat/generate` 生成测试方案。
5. 结果展示：展示测试范围、风险提示和结构化测试用例。
6. 方案导出：支持复制方案和导出 Markdown。

## 数据库初始化

旧的项目、需求、用例管理接口仍保留，使用 MySQL 初始化脚本：

```bash
mysql -u root -p < database/schema.sql
```

默认数据库名为：

```text
ai_test_case
```

如需修改数据库账号密码，请设置环境变量：

```powershell
$env:MYSQL_USERNAME="root"
$env:MYSQL_PASSWORD="123456"
```

## DeepSeek 配置

后端默认开启 mock 模式，即使没有 DeepSeek API Key，也能生成演示方案。

接入真实 DeepSeek 时，请只在后端环境变量里配置，不要写入前端代码、配置文件或提交到 GitHub：

```powershell
$env:DEEPSEEK_API_KEY="你的_API_KEY"
$env:DEEPSEEK_MOCK_ENABLED="false"
```

可选配置：

```powershell
$env:DEEPSEEK_BASE_URL="https://api.deepseek.com"
$env:DEEPSEEK_MODEL="deepseek-chat"
```

## 启动后端

进入后端目录：

```bash
cd backend
mvn spring-boot:run
```

后端默认地址：

```text
http://localhost:8080
```

接口文档地址：

```text
http://localhost:8080/swagger-ui.html
```

## 启动前端

进入前端目录：

```bash
cd frontend
npm install
npm run dev
```

前端默认地址：

```text
http://localhost:5173
```

## 测试命令

后端测试：

```bash
cd backend
mvn test
```

前端测试：

```bash
cd frontend
npm test
```

前端构建：

```bash
cd frontend
npm run build
```

## 基础层验收演示流程

1. 打开前端首页。
2. 在输入框描述要测试的模块，例如“我想测试登录模块”。
3. 等待 DeepSeek 根据模块生成补充问题，并按步骤选择回答。
4. 点击确认生成。
5. 查看 AI 输出的测试范围、风险提示和测试用例。
6. 复制或导出 Markdown 方案。
