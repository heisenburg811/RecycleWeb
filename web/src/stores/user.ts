import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { apiLogin, apiLogout, apiRegister, fetchMe } from '@/api/auth'
import { REFRESH_KEY, TOKEN_KEY } from '@/api/request'
import type { LoginVO, UserVO } from '@/api/types'

export const useUserStore = defineStore('user', () => {
  const accessToken = ref<string>(localStorage.getItem(TOKEN_KEY) ?? '')
  const user = ref<UserVO | null>(null)

  const isLogin = computed(() => Boolean(accessToken.value))

  function setAuth(res: LoginVO) {
    accessToken.value = res.accessToken
    user.value = res.user
    localStorage.setItem(TOKEN_KEY, res.accessToken)
    localStorage.setItem(REFRESH_KEY, res.refreshToken)
  }

  async function login(phone: string, password: string) {
    setAuth(await apiLogin(phone, password))
  }

  async function register(phone: string, password: string, nickname?: string) {
    setAuth(await apiRegister(phone, password, nickname))
  }

  /** 先去服务端撤销 refresh token，再清本地（失败也要清本地） */
  async function logout() {
    const refreshToken = localStorage.getItem(REFRESH_KEY)
    try {
      if (refreshToken) await apiLogout(refreshToken)
    } catch {
      // 网络异常不阻塞登出
    } finally {
      accessToken.value = ''
      user.value = null
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(REFRESH_KEY)
    }
  }

  async function loadMe() {
    if (!accessToken.value) return
    user.value = await fetchMe()
  }

  return { accessToken, user, isLogin, setAuth, login, register, logout, loadMe }
})
