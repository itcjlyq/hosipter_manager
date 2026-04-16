# 患者端 uni-app（登录 + 主体框架）

风格：薄荷绿主色、白卡片、圆角与轻阴影，贴近设计稿中的首页布局。

## 如何运行

1. 使用 **HBuilderX**（推荐）：`文件` → `导入` → `从本地目录导入`，选择本目录 `patient-uniapp`，然后运行到 **浏览器（H5）** 或 **微信开发者工具**。

### 微信小程序（避免 `path ... Received undefined`）

1. **必须先编译出小程序包**：在 HBuilderX 中选 **运行 → 运行到小程序模拟器 → 微信开发者工具**（或发行到微信小程序），生成目录  
   `unpackage/dist/dev/mp-weixin/`（开发）或 `unpackage/dist/build/mp-weixin/`（发行）。
2. **导入目录二选一**（不要导入整个「用户管理工具」仓库根目录，也不要导入 `admin-web`）：
   - **推荐 A**：只导入 **`patient-uniapp/unpackage/dist/dev/mp-weixin`**（纯小程序结构，最稳）；或  
   - **推荐 B**：导入 **`patient-uniapp`** 根目录（本仓库已加根目录 `project.config.json`，其中 `miniprogramRoot` 指向上述编译产物；**同样要求先完成一次编译**，否则该目录不存在仍会报错）。
3. **路径含中文** 时，部分版本微信开发者工具会异常；若仍报错，请将 `patient-uniapp` **复制到英文路径**（如 `D:\dev\patient-uniapp`）再编译、再导入。
4. 根目录 `project.config.json` 里的 **`appid`** 需与你在微信公众平台的小程序一致；测试可先用测试号或修改为你的 AppID。
2. **Vue3 + Vite 跑 H5** 时，工程根目录必须包含 **`index.html`**（本仓库已提供），作为 Web 入口页。
3. 若你使用 **uni-app 官方脚手架**创建工程，可将本目录下 `pages`、`components`、`utils`、`App.vue`、`main.js`、`pages.json`、`manifest.json`、`uni.scss`、`index.html` 合并进工程。

## 登录与路由

- 启动后首屏为 **`pages/gate/gate`**：两个入口 — **患者端登录**、**医生端登录**。
- 患者端：`patient-login` → 校验 JWT `role === patient` → 进入 **`pages/home/home`**。
- 医生端：`doctor-login` → 校验 JWT `role === doctor` → 进入 **`pages/doctor/home`**。
- 入口选错（例如医生账号走患者端）会提示并拒绝写入登录态。

## 后端地址

编辑 `utils/config.js` 中的 `API_BASE`：

- **H5（带代理）**：`manifest.json` 已示例将 `/api` 代理到 `http://localhost:8080`，此时 `API_BASE` 可保持为 `''`，请求路径以 `/api` 开头即可。
- **微信小程序**：`utils/config.js` 里 **`MP_HOST`**（条件编译 `MP-WEIXIN`）必须改成你本机 `ipconfig` 中的 IPv4，例如 `http://192.168.1.10:8080`。若仍为 `http://192.168.1.100:8080` 等占位地址且本机不是该 IP，开发者工具控制台会出现 **`Error: timeout`**，登录会一直失败。
- **真机 / App**：同样不要写 `localhost`，需与电脑同一局域网且防火墙放行后端端口。

后端已增加 **CORS** 配置，便于 H5 联调。

## 页面说明

- `pages/gate/gate`：双入口
- `pages/login/patient-login`、`pages/login/doctor-login`：分别调用 `/api/auth/login/password` 并校验角色
- `pages/home/home`：患者首页；`pages/doctor/home`：医生首页（占位）
- 其余为快捷入口占位页，便于后续接业务

各端首页在 `onShow` 校验 token 与角色，不符则清空并回入口页。
