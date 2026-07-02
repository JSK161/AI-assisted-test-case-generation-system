# AI 辅助测试用例生成系统 — 微信小程序

## 目录结构

```
weapp/
├── app.js                     # 小程序入口
├── app.json                   # 全局配置（含 tabBar）
├── app.wxss                   # 全局样式
├── project.config.json        # 项目配置
├── sitemap.json
├── images/                    # tabBar 图标（需自行添加）
│   ├── tab_chat.png
│   ├── tab_chat_hl.png
│   ├── tab_history.png
│   ├── tab_history_hl.png
│   ├── tab_profile.png
│   └── tab_profile_hl.png
├── utils/
│   ├── auth.js                # Token/用户信息管理
│   ├── request.js             # HTTP 请求封装
│   └── util.js                # 工具函数
└── pages/
    ├── login/                 # 登录页
    ├── register/              # 注册页
    ├── chat/                  # AI 对话页（核心）
    ├── history/               # 对话历史
    └── profile/               # 个人中心
```

## 使用步骤

### 1. 导入微信开发者工具

打开 **微信开发者工具** → **导入项目** → 选择 `weapp/` 目录 → **确定**

### 2. 添加 tabBar 图标

在 `images/` 目录下放置 6 张 48x48 PNG 图标：

| 文件名 | 说明 |
|--------|------|
| `tab_chat.png` | AI 对话（未选中） |
| `tab_chat_hl.png` | AI 对话（选中） |
| `tab_history.png` | 历史（未选中） |
| `tab_history_hl.png` | 历史（选中） |
| `tab_profile.png` | 我的（未选中） |
| `tab_profile_hl.png` | 我的（选中） |

> 可临时移除 `app.json` 中的 `iconPath` 和 `selectedIconPath` 跳过图标设置

### 3. 配置后端地址

小程序默认连接 `http://localhost:8080/api`，如需修改请编辑 `app.js`：

```js
globalData: {
  baseUrl: 'http://你的服务器IP:8080/api'  // 开发时用局域网IP
}
```

> **注意**：微信开发者工具中需关闭「校验合法域名」：
> 详情 → 本地设置 → 勾选「不校验合法域名、web-view（业务域名）、TLS 版本以及 HTTPS 证书」

### 4. 编译运行

点击 **编译** 即可，默认进入登录页，使用 `admin / 123456` 登录。

## 功能清单

| 页面 | 功能 |
|------|------|
| 登录 | 用户名密码登录 |
| 注册 | 注册新账号（含邮箱） |
| AI 对话 | 需求输入、分类选择、文件上传、补充问题、生成方案 |
| 历史 | 对话列表、点击继续对话、左滑删除 |
| 我的 | 头像、邮箱修改、密码修改、管理员用户管理、退出登录 |

## 注意

- 后端需先启动（`mvn spring-boot:run`）
- 小程序与后端在同一网络下才能正常通信
- 文件上传仅支持文本格式（.txt, .md, .json, 代码文件等）
