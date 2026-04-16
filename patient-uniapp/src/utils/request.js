import { API_BASE } from './config.js'
import { clearAuth, getToken } from './auth.js'

function joinUrl(path) {
  if (!path.startsWith('/')) {
    return `${API_BASE}/${path}`
  }
  return `${API_BASE}${path}`
}

/**
 * JSON API（统一 { code, message, data }）
 */
export function requestJson({ url, method = 'GET', data = null, header = {} }) {
  const token = getToken()
  const h = {
    'Content-Type': 'application/json',
    ...header
  }
  if (token) {
    h.Authorization = `Bearer ${token}`
  }
  return new Promise((resolve, reject) => {
    const fullUrl = joinUrl(url)
    // 微信小程序必须用完整 http(s) 地址；H5 可用 /api 走 devServer 代理
    let mpNeedAbsolute = false
    // #ifdef MP-WEIXIN
    mpNeedAbsolute = true
    // #endif
    if (
      mpNeedAbsolute &&
      fullUrl &&
      !fullUrl.startsWith('http') &&
      !fullUrl.startsWith('//')
    ) {
      reject(
        new Error(
          '请配置 utils/config.js：微信小程序需填写本机局域网 IP（不能用 localhost）'
        )
      )
      return
    }
    uni.request({
      url: fullUrl,
      method,
      data,
      header: h,
      timeout: 30000,
      success: (res) => {
        const body = res.data
        const status = res.statusCode
        // 4xx/5xx 仍走 success：必须先按 HTTP 状态处理，否则会误 resolve 成「缺少 token」
        if (status === 401 || status === 403) {
          clearAuth()
          reject(new Error(body?.message || '无权限'))
          return
        }
        if (status < 200 || status >= 300) {
          const hint =
            typeof body === 'object' && body != null && body.message
              ? String(body.message)
              : typeof body === 'string' && body.length < 200
                ? body.trim()
                : ''
          reject(
            new Error(
              hint
                ? `请求失败（HTTP ${status}）：${hint}`
                : `请求失败（HTTP ${status}）：请确认后端已在本机启动，且 config 中的 IP 为本机当前网段地址（502 多为网关或错误 IP）`
            )
          )
          return
        }
        if (typeof body?.code === 'number' && body.code !== 0) {
          reject(new Error(body.message || '请求失败'))
          return
        }
        resolve(body)
      },
      fail: (err) => {
        const msg =
          err?.errMsg ||
          err?.message ||
          (typeof err === 'string' ? err : '') ||
          '网络请求失败'
        if (msg.includes('timeout') || msg.includes('超时')) {
          reject(new Error('请求超时：请检查后端是否启动、手机与电脑是否同一局域网、utils/config.js 中 IP 是否正确'))
        } else {
          reject(new Error(msg))
        }
      }
    })
  })
}
