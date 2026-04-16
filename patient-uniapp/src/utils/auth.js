const TOKEN_KEY = 'accessToken'
const EXPIRES_KEY = 'tokenExpiresAt'
const ROLE_KEY = 'userRole'

export function getToken() {
  return uni.getStorageSync(TOKEN_KEY) || ''
}

export function setToken(token, expiresAtSec) {
  uni.setStorageSync(TOKEN_KEY, token || '')
  if (expiresAtSec) {
    uni.setStorageSync(EXPIRES_KEY, expiresAtSec)
  }
}

/** 登录端类型：patient / doctor / admin */
export function setUserRole(role) {
  if (role) {
    uni.setStorageSync(ROLE_KEY, role)
  } else {
    uni.removeStorageSync(ROLE_KEY)
  }
}

export function getUserRole() {
  return uni.getStorageSync(ROLE_KEY) || ''
}

export function clearAuth() {
  uni.removeStorageSync(TOKEN_KEY)
  uni.removeStorageSync(EXPIRES_KEY)
  uni.removeStorageSync(ROLE_KEY)
}

export function isLoggedIn() {
  return !!getToken()
}
