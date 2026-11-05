import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { apiLogin, apiRegister, fetchMe } from '@/api/auth'
import { TOKEN_KEY } from '@/api/request'
import type { UserVO } from '@/api/types'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem(TOKEN_KEY) ?? '')
  const user = ref<UserVO | null>(null)

  const isLogin = computed(() => Boolean(token.value))

  function setAuth(newToken: string, newUser: UserVO) {
    token.value = newToken
    user.value = newUser
    localStorage.setItem(TOKEN_KEY, newToken)
  }

  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem(TOKEN_KEY)
  }

  async function login(phone: string, password: string) {
    const res = await apiLogin(phone, password)
    setAuth(res.token, res.user)
  }

  async function register(phone: string, password: string, nickname?: string) {
    const res = await apiRegister(phone, password, nickname)
    setAuth(res.token, res.user)
  }

  async function loadMe() {
    if (!token.value) return
    user.value = await fetchMe()
  }

  return { token, user, isLogin, setAuth, logout, login, register, loadMe }
})
