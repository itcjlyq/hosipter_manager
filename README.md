## 项目说明：用户管理工具（医院服务）

本项目包含一个基于 **Spring Boot 3 / MyBatis-Plus / JWT** 的后端服务，以及一个基于 **uni-app + Vue 3** 的前端小程序 / H5，用于实现医院场景下的用户与诊疗资料管理，包括患者、医生和管理员三类角色。

---

### 1. 项目结构

- **backend**：后端服务，提供统一的 REST API
  - `pom.xml`：Maven 配置（Spring Boot 3.3.13，Java 17，MyBatis-Plus，jjwt 等）
  - `src/main/java/com/hospital/backend`：
    - `HospitalBackendApplication`：Spring Boot 启动类
    - `auth`：登录认证、JWT 生成与校验（`AuthController`、`AuthService`、`JwtTokenService` 等）
    - `config`：安全配置（`SecurityConfig`）、JWT 过滤器（`JwtAuthenticationFilter`）、文件存储配置（`FileStorageProperties`）
    - `admin`：管理员管理医生 / 患者的接口（创建、查询、修改、重置密码、删除等）
    - `profile`：医生 / 患者个人资料接口及 VO / Mapper
    - `bind`：医患绑定（申请、邀请、审核、拒绝等）
    - `file`：患者诊断档案附件上传 / 下载 / 删除相关接口
    - `user`：系统用户实体与 Mapper（`SysUser`、`SysUserMapper`）
    - `common`：统一返回体 `ApiResponse`、全局异常处理 `GlobalExceptionHandler`、安全工具 `SecurityUtils` 等
    - `web`：通用健康检查接口（`HealthController`）
  - `src/main/resources`：
    - `application.yml`：服务端口、数据源、JWT、文件存储等配置
    - `sql/`：数据库初始化与增量脚本（详见其中 `README.md`）

- **patient-uniapp**：前端患者 / 医生小程序 / H5 工程
  - `package.json`：依赖与脚本（使用 Vite + uni-app）
  - `src/pages.json`：路由配置，声明所有页面路径与导航栏样式
  - `src/App.vue`：应用入口与全局样式（包含 H5 端根节点高度处理、字体等）
  - 典型页面：
    - `pages/gate/gate`：角色入口页，选择「患者端」或「医生端」并跳转登录
    - `pages/login/patient-login` / `pages/login/doctor-login`：分别对应患者与医生登录
    - `pages/home/home`：患者首页，展示个人资料、医患绑定状态、快捷入口（上传档案 / 就诊记录 / 在线咨询 / 个人中心）
    - `pages/patient/bind-doctor`：患者管理绑定医生
    - `pages/patient/archives`：患者已上传诊断档案列表（可查看 / 删除）
    - `pages/doctor/home`：医生工作台首页（患者管理、在线咨询、诊断记录等入口）
    - `pages/doctor/my-patients`：医生端患者管理
    - `pages/doctor/chat` / `pages/chat/chat`：在线咨询相关页面
    - `pages/doctor/records` / `pages/records/records`：诊断 / 就诊记录列表
    - `pages/upload/upload`：上传诊断档案（图片、PDF 等）
    - `pages/profile/profile`：个人中心

---

### 2. 角色与主要功能

- **管理员（ADMIN）**
  - 登录后可通过 `/api/admin/**` 接口管理系统用户：
    - `AdminPatientController`：创建 / 查询 / 编辑 / 删除患者，重置患者密码
    - `AdminDoctorController`：创建 / 查询 / 编辑 / 删除医生，重置医生密码
- **医生（DOCTOR）**
  - 登录后访问 `/api/doctor/**`：
    - 查看医患绑定列表（待审核 / 已通过 / 已拒绝）
    - 向患者发起绑定邀请、审核或拒绝患者的绑定申请
    - 后续可扩展诊断记录、在线咨询等相关接口
- **患者（PATIENT）**
  - 登录后访问 `/api/patient/**`：
    - `PatientMeController`：获取当前登录患者个人资料（姓名、脱敏手机号、性别、年龄等）
    - `PatientFileController`：管理自己的诊断档案附件（上传、列表、下载、删除）
    - 通过医患绑定接口与医生进行关联

---

### 3. 后端安全与认证说明

- **认证方式**
  - 使用 `AuthController` 提供的接口：`POST /api/auth/login/password`
  - 入参 `LoginRequest` 通常包含手机号和密码，校验通过后：
    - 生成 JWT：`jwtTokenService.generate(userId, roleName)`
    - 返回 `LoginResponse`：包含 `token` 以及 `expiresAt`（过期时间戳）
- **鉴权与角色控制**
  - 在 `SecurityConfig` 中配置：
    - 公开接口：`/health`、`/api/health`、`/api/auth/login/password`、`/actuator/health`
    - 角色控制：
      - `/api/admin/**` 仅 `ADMIN` 角色可访问
      - `/api/doctor/**` 仅 `DOCTOR` 角色可访问
      - `/api/patient/**` 仅 `PATIENT` 角色可访问
    - 其余接口默认需要认证
  - 通过 `JwtAuthenticationFilter` 从 `Authorization` Header 中解析 JWT 并注入安全上下文
- **全局返回结构**
  - 所有接口统一返回 `ApiResponse<T>`：
    - `code`：业务状态码（0 表示成功，非 0 表示失败）
    - `message`：提示信息
    - `data`：具体业务数据

---

### 4. 数据库与初始化脚本

- 数据源配置见 `backend/src/main/resources/application.yml`：
  - 数据库：`hospital_service`
  - 默认使用 MySQL（5.7 / 8.0 均兼容），建议按需要修改 URL、用户名、密码
- 初始化脚本位于 `backend/src/main/resources/sql`，并已在 `README.md` 中给出执行顺序：
  1. `001_init_schema.sql`：创建库 `hospital_service`，以及基础表 `sys_user`、`doctor_profile`、`patient_profile`
  2. `002_doctor_patient_bind.sql`：医患绑定表 `doctor_patient_bind`
  3. `003_patient_file_asset.sql`：患者附件元数据表 `patient_file_asset`
  4. `004_admin_web_no_new_tables.sql`：管理端 Web 相关说明与可选索引（不新增表）
- **执行建议**
  - 新环境：按照上表 **从 1 到 4 依次执行**
  - 已有环境：只执行「尚未执行过」且序号更大的新脚本（作为增量）

> 提示：生产环境不建议在 `application.yml` 中硬编码数据库账号和 JWT 密钥，推荐通过环境变量或外部配置覆盖。

---

### 5. 文件存储与诊断档案

- 在 `application.yml` 中：
  - `app.storage.root: upload`：附件根目录，相对 JVM 启动时的工作目录
  - 图片会写入 `upload/images/`，其他文件写入 `upload/files/`（逻辑见 `LocalFileStorage`）
- 患者端相关接口（`PatientFileController`）：
  - `POST /api/patient/files`：上传附件（`multipart/form-data`），支持附带业务类型 `bizType`
  - `GET /api/patient/files`：列出当前患者已上传的所有附件
  - `GET /api/patient/files/{id}/download`：下载指定附件
  - `DELETE /api/patient/files/{id}`：软删除附件
- 前端在 `pages/upload/upload` / `pages/patient/archives` 中对接上述接口，支持预览图片、用系统文档查看器打开 PDF / 文档等。

---

### 6. 前端说明（patient-uniapp）

- 技术栈：**uni-app + Vue 3 + Vite**
- 脚本命令（在 `patient-uniapp` 目录下执行）：
  - `npm install`：安装依赖
  - `npm run dev:h5`：以 H5 方式本地调试
  - `npm run dev:mp-weixin`：以微信小程序方式本地调试 / 真机预览
  - `npm run build:h5` / `npm run build:mp-weixin`：分别构建 H5 / 小程序产物
- 路由与页面：
  - 在 `src/pages.json` 中维护，包含所有页面路径与导航栏配置
  - 通过 `uni.navigateTo`、`uni.reLaunch` 等 API 进行页面跳转
- 鉴权与角色识别：
  - 前端使用 `utils/auth.js` 持久化 JWT、用户角色等信息
  - 从 JWT 中解析 `role` 字段，区分 `patient` 与 `doctor`，并在页面 `onShow` 中进行校验：
    - 若角色不匹配当前端（如用医生账号访问患者页），会清理登录态并引导回入口页

---

### 7. 启动步骤（本地开发）

1. **准备数据库**
   - 在本地或远程 MySQL 中创建数据库 `hospital_service`（或按需修改脚本与配置）
   - 按 `backend/src/main/resources/sql/README.md` 中的顺序执行 `001`～`004` 脚本
2. **配置后端**
   - 进入 `backend` 目录，根据本地环境修改 `src/main/resources/application.yml` 中的：
     - `spring.datasource.url`
     - `spring.datasource.username`
     - `spring.datasource.password`
     - 以及 `security.jwt.secret`（推荐自定义并保密）
3. **启动后端**
   - 在 `backend` 目录执行：
     - `mvn spring-boot:run`
   - 默认启动端口：`http://localhost:8080`
   - 可通过：
     - `GET /health` 或 `GET /api/health` 检查服务是否正常
4. **启动前端**
   - 进入 `patient-uniapp` 目录：
     - `npm install`
     - H5 调试：`npm run dev:h5`
     - 微信小程序：`npm run dev:mp-weixin`，再用微信开发者工具导入 `dist/dev/mp-weixin` 目录
   - 确保前端配置的后端 API 地址指向已启动的后端服务（若使用代理或环境变量，请按本地实际情况调整）。

---

### 8. 后续可扩展方向

- 补充医生端的诊断记录管理、在线咨询的实时通信能力（如 WebSocket / IM 服务对接）
-,完善管理员 Web 端（如单独的管理后台前端工程）
- 增加操作审计日志、敏感数据脱敏、更多权限粒度控制等

本说明文档面向开发与运维同学，帮助快速理解本项目的总体架构、模块划分与启动方式。若后续新增模块或接口，建议同步更新本 `README.md`。

