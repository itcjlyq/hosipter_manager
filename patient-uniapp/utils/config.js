/**
 * 后端 API 根地址（不要末尾斜杠）。
 *
 * - H5：保持 ''，由 manifest 里 h5.devServer.proxy 把 /api 转到本机后端。
 * - 微信小程序 / App：不能使用 localhost，必须写「运行 Spring Boot 的那台电脑」的局域网 IP。
 *
 * 下面用条件编译：仅微信小程序走 MP_HOST，其余端仍用空字符串（H5 代理）。
 *
 * 【重要】请把 MP_HOST 改成你本机 ipconfig 查到的 IPv4，例如 http://192.168.31.8:8080
 */
// #ifdef MP-WEIXIN
const MP_HOST = 'http://127.0.0.1:8080'
export const API_BASE = MP_HOST
// #endif

// #ifndef MP-WEIXIN
export const API_BASE = ''
// #endif

/** 为 true 时患者/医生登录页自动填充下方测试账号（正式发版前请改为 false） */
export const DEV_PREFILL_LOGIN = true

export const DEV_LOGIN_PATIENT = Object.freeze({
  phone: '13800000002',
  password: '123456'
})

export const DEV_LOGIN_DOCTOR = Object.freeze({
  phone: '13800000001',
  password: '123456'
})
