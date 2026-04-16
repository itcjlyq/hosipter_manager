# 医院管理后台（Vue 3 + Element Plus）

清新亮色主题（薄荷青主色），对接现有 Spring Boot 接口：`/api/auth/login/password`、`/api/admin/**`。

## 环境要求

- Node.js 18+
- 后端 `hospital-backend` 已启动（默认 `http://localhost:8080`）

## 安装与运行

```bash
cd admin-web
npm install
npm run dev
```

浏览器访问控制台输出的本地地址（Vite 默认 `http://localhost:5174`）。  
开发环境通过 `vite.config.js` 将 `/api` **代理**到 `8080`，无需改跨域。

## 构建生产包

```bash
npm run build
```

将 `dist` 部署到 Nginx 等静态资源服务器；若前后端不同域，请配置反向代理把 `/api` 指到后端，或设置环境变量 `VITE_API_BASE` 为后端完整根地址。

## 登录说明

请使用 **管理员** 账号（`sys_user.role = 1`）。默认首次启动可在后端 `application.yml` 的 `app.bootstrap.admin` 中查看手机号与密码。

## 功能

- 概览：医生 / 患者 / 绑定数量（列表长度）
- 医生管理：增删改、启用禁用、重置密码
- 患者管理：同上
- 医患绑定：只读列表 + 状态筛选

数据库脚本见后端 `src/main/resources/sql/`，管理端不新增表，执行到 `004` 说明文件即可。
