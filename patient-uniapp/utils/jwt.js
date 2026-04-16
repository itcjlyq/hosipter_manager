/**
 * 解析 JWT payload（仅用于读取 role 等声明，不校验签名）。
 * 依赖运行环境提供 atob；多数 H5 / App 可用。
 */
export function parseJwtPayload(token) {
  if (!token || typeof token !== 'string') {
    return null
  }
  const parts = token.split('.')
  if (parts.length < 2) {
    return null
  }
  try {
    let base64 = parts[1].replace(/-/g, '+').replace(/_/g, '/')
    const pad = base64.length % 4
    if (pad === 2) {
      base64 += '=='
    } else if (pad === 3) {
      base64 += '='
    }
    const raw = atob(base64)
    const json = decodeURIComponent(
      raw
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    )
    return JSON.parse(json)
  } catch (e) {
    return null
  }
}
