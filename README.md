<!-- <div style="display: flex; justify-content: center; align-items: center; width: 100%;">
  <img src="./favicon.ico" alt="Vibe Music Icon" width="100">
  <h1>Vibe Music</h1>
</div> -->
<!-- --- -->
<div style="display: flex; flex-direction: column; justify-content: center; align-items: center; width: 100%;">
  <img src="./favicon.ico" alt="Vibe Music Icon" width="100">
  <h1>Vibe Music</h1>

</div>

<p align="center">
  <img src="https://img.shields.io/badge/Spring%20AI-1.1.2-6DB33F?logo=spring" alt="Spring AI">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.5.16-6DB33F?logo=springboot" alt="Spring Boot">
  <img src="https://img.shields.io/badge/MyBatis-3.5.9-000000" alt="MyBatis Plus">
  <img src="https://img.shields.io/badge/JDK-17+-orange?logo=openjdk" alt="JDK 17+">
  <img src="https://img.shields.io/badge/Maven-3.8+-C71A36?logo=apache-maven" alt="Maven">
</p>

<p align="center">
  <img src="https://img.shields.io/badge/MySQL-8.0+-4479A1?logo=mysql" alt="MySQL">
  <img src="https://img.shields.io/badge/Redis-7.0+-DC382D?logo=redis" alt="Redis">
  <img src="https://img.shields.io/badge/MinIO-250907T16-00C7B1?logo=minio" alt="MinIO">
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Node.js-18+-339933?logo=node.js" alt="Node.js">
  <img src="https://img.shields.io/badge/pnpm-9.15.2+-F69220?logo=pnpm" alt="pnpm">
</p>
<p align="center">
  <a href="https://github.com/your-username/vibe-music/blob/main/LICENSE">
    <img src="https://img.shields.io/badge/license-MIT-blue" alt="license MIT">
  </a>
</p>

---

# 

Vibe Music 是一个前后端分离的在线音乐平台，包含**用户端音乐播放器**、**后台管理系统**和 **Spring Boot 后端服务**三大部分。用户可以在线搜索、播放、收藏和评论音乐，管理员可以在后台维护曲库与用户数据，平台还内置了一个基于大模型的 AI 音乐助手（VibeAgent）。

## 功能特性 ✨

### 用户端（vibe-music-client）

- 浏览与搜索歌曲、歌手、歌单，支持核心播放控制
- 用户注册 / 登录（JWT），个人资料编辑与头像上传
- 收藏歌曲与歌单、发表评论与点赞、歌曲下载
- 基于听歌历史的个性化推荐
- 暗黑模式切换

### 管理端（vibe-music-admin）

- 用户管理（查看、启用 / 禁用账号）
- 歌手、歌曲、歌单的增删改查与资源管理
- 轮播图管理、用户反馈处理

### 后端（vibe-music-backend）

- RESTful API + JWT 认证，基于角色的路径权限控制（ROLE_ADMIN / ROLE_USER）
- MinIO 对象存储管理音频、图片等媒体文件上传
- Redis 缓存（注解缓存统一 10 分钟有效期）、邮件服务
- **VibeAgent**：基于 Spring AI + 阿里云百炼（DashScope）的 AI 音乐助手，可通过工具调用查询真实曲库（按关键词搜歌、按风格推荐、热门歌曲），支持 SSE 流式对话与多轮会话记忆

## 技术栈 🛠️

| 部分 | 技术 |
| --- | --- |
| 用户端 / 管理端 | Vue 3、TypeScript、Vite、Pinia、Element Plus、Tailwind CSS、Axios、Artplayer |
| 管理端模板 | [vue-pure-admin](https://github.com/pure-admin/vue-pure-admin)、ECharts |
| 后端框架 | Spring Boot 3.5、Java 17、Maven 多模块 |
| 数据层 | MyBatis-Plus、MySQL、Druid 连接池、Redis |
| 安全 | JWT（auth0 java-jwt）、角色路径权限映射 |
| 存储 / 服务 | MinIO 对象存储、Jakarta Mail 邮件服务 |
| AI | Spring AI 1.1、Spring AI Alibaba（DashScope / 通义千问） |
| 包管理 | pnpm（前端）、Maven（后端） |

## 整体结构 📂

```
vibe-music/
├── vibe-music-backend/          # 后端（Maven 多模块）
│   ├── vibe-music-server/       # 主应用：controller / service / mapper / config / agent，可执行 fat jar
│   ├── vibe-music-model/        # 数据模型：entity / dto / vo
│   └── vibe-music-common/       # 公共模块：常量 / 枚举 / 统一返回结果 / 工具类
├── vibe-music-client/           # 用户端前端（Vue 3 + Vite）
├── vibe-music-admin/            # 管理端前端（基于 vue-pure-admin）
```

## 项目演示 🤩
[演示文档](./docs/演示.md)

## 快速开始 🚀

> 更详细的文档：[如何开始](./docs/start.md)

### 环境要求

- JDK 17+、Maven
- Node.js ≥ 18、pnpm≥9
- MySQL、Redis、MinIO

### 启动后端

```bash
cd vibe-music-backend
# 修改 vibe-music-server/src/main/resources/application-dev.yml 中的
# MySQL / Redis / MinIO / 邮箱 / DashScope API Key 配置
./mvnw spring-boot:run -pl vibe-music-server
```

### 启动用户端

```bash
cd vibe-music-client
pnpm install
pnpm dev
```

### 启动管理端

```bash
cd vibe-music-admin
pnpm install
pnpm dev
```

<br>
<br>

> 说明: 项目是基于 [Alex-LiSun](https://github.com/Alex-LiSun)的开源项目VibeMusic进行开发
> 📄: [MIT License](./LICENSE)

