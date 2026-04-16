import { requestJson } from './request.js'
import { setToken, setUserRole } from './auth.js'
import { parseJwtPayload } from './jwt.js'

/**
 * @param {string} expectedRole  'patient' | 'doctor' — 与后端 JWT 中 claim `role` 一致
 */
export async function loginWithPassword(phone, password, expectedRole) {
  const res = await requestJson({
    url: '/api/auth/login/password',
    method: 'POST',
    data: { phone, password }
  })
  const token = res.data?.accessToken
  const exp = res.data?.expiresAt
  if (!token) {
    throw new Error('登录响应缺少 token')
  }
  const payload = parseJwtPayload(token)
  const role = payload?.role
  if (expectedRole && role !== expectedRole) {
    if (expectedRole === 'patient') {
      throw new Error('该账号不是患者账号，请使用「医生端登录」入口')
    }
    if (expectedRole === 'doctor') {
      throw new Error('该账号不是医生账号，请使用「患者端登录」入口')
    }
  }
  setToken(token, exp)
  setUserRole(role || '')
  return { token, role }
}
