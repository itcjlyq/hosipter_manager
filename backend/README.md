# hospital-backend

后端工程（持续迭代中）：

- Spring Boot 3 + Java 17
- MyBatis-Plus
- JWT 登录（数据库账号 + BCrypt）
- 管理员接口：`/api/admin/**`（需 `ROLE_ADMIN`）
- `doctor_profile` / `patient_profile` 表与后台 CRUD
- MySQL 5.7 优先，兼容 8.0 连接参数

## 1. 启动前准备

1. 安装 JDK 17
2. 安装 Maven 3.9+
3. 按顺序执行数据库脚本：见 [`src/main/resources/sql/README.md`](src/main/resources/sql/README.md)（当前从 `001_init_schema.sql` 开始）

## 2. 启动

```bash
mvn clean spring-boot:run
```

首次启动会自动创建管理员（若不存在）：手机号 `13800000000`，密码 `admin123456`（可在 `application.yml` 的 `app.bootstrap.admin` 修改）。

## 3. 接口验证

### 3.1 健康检查（无需登录）

```bash
curl http://localhost:8080/api/health
```

### 3.2 管理员登录

```bash
curl -X POST http://localhost:8080/api/auth/login/password ^
  -H "Content-Type: application/json" ^
  -d "{\"phone\":\"13800000000\",\"password\":\"admin123456\"}"
```

将返回 JSON 中的 `data.accessToken` 作为 `TOKEN`。

### 3.3 管理端：医生 CRUD（需管理员 Token）

- `POST /api/admin/doctors` 创建医生（body：`phone`、`password`、`name`，可选 `doctorNo`、`dept`、`title`、`intro`）
- `GET /api/admin/doctors` 列表
- `GET /api/admin/doctors/{userId}` 详情
- `PUT /api/admin/doctors/{userId}` 更新档案与账号状态（`status`：0 禁用 / 1 启用）
- `POST /api/admin/doctors/{userId}/reset-password` body：`{"newPassword":"..."}`
- `DELETE /api/admin/doctors/{userId}` 软删除

### 3.4 管理端：患者 CRUD（需管理员 Token）

- `POST /api/admin/patients` 创建患者（body：`phone`、`password`、`name`，可选 `gender`、`birthday`、`idCardMask`）
- `GET /api/admin/patients` 列表
- `GET /api/admin/patients/{userId}` 详情
- `PUT /api/admin/patients/{userId}` 更新
- `POST /api/admin/patients/{userId}/reset-password`
- `DELETE /api/admin/patients/{userId}` 软删除

示例（创建医生，Windows PowerShell 注意转义）：

```bash
curl -X POST http://localhost:8080/api/admin/doctors -H "Content-Type: application/json" -H "Authorization: Bearer TOKEN" -d "{\"phone\":\"13900000001\",\"password\":\"123456\",\"name\":\"张医生\",\"dept\":\"整形科\"}"
```

非管理员访问 `/api/admin/**` 将返回 HTTP 403，JSON：`{"code":-1,"message":"无权限访问","data":null}`。

## 4. 医患绑定与附件（Step 4～5）

- **绑定**：患者 `GET /api/patient/doctors`、`POST /api/patient/binds`；医生 `GET /api/doctor/binds`、`approve`/`reject`/`invite`；管理 `GET /api/admin/binds`（均需对应角色 JWT）。
- **附件（本地磁盘）**：执行 `003_patient_file_asset.sql`；配置 `app.storage.root`。患者 `POST/GET/DELETE /api/patient/files`，下载 `GET /api/patient/files/{id}/download`。医生对某患者 `POST/GET/DELETE /api/doctor/files`（表单/参数带 `patientUserId`），下载 `GET /api/doctor/files/{id}/download`。**医生侧操作要求与该患者已有「已通过」绑定。**

## 5. 管理端 Web（Vue3 + Element Plus）

仓库内 `../admin-web`：管理员登录、医生/患者 CRUD、医患绑定只读列表。开发时 `npm run dev`（Vite 代理 `/api` 到本机 `8080`）。数据库脚本含 `004_admin_web_no_new_tables.sql`（说明项，无新表）。

## 6. 更后续（可选）

- 站内消息 / 通知、聊天记录、预约等
