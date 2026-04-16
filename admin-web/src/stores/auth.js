import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

const TOKEN_KEY = 'admin_token'
const EXP_KEY = 'admin_token_exp'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem(TOKEN_KEY) || '')
  const expiresAt = ref(Number(localStorage.getItem(EXP_KEY) || 0))

  const isLoggedIn = computed(() => !!token.value)

  function setSession(accessToken, expSec) {
    token.value = accessToken || ''
    expiresAt.value = expSec ? Number(expSec) : 0
    if (accessToken) {
      localStorage.setItem(TOKEN_KEY, accessToken)
    } else {
      localStorage.removeItem(TOKEN_KEY)
    }
    if (expSec) {
      localStorage.setItem(EXP_KEY, String(expSec))
    } else {
      localStorage.removeItem(EXP_KEY)
    }
  }

  function clear() {
    setSession('', 0)
  }

  return { token, expiresAt, isLoggedIn, setSession, clear }
})
