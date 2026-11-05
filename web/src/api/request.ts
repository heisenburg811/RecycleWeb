import axios from 'axios'
import type { AxiosError, InternalAxiosRequestConfig } from 'axios'
import { showToast } from 'vant'
import type { LoginVO } from './types'

interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export const TOKEN_KEY = 'recycle_token'
export const REFRESH_KEY = 'recycle_refresh'

const http = axios.create({
  baseURL: '/api/v1',
  timeout: 15000
})

http.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY)
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

/** 单飞：并发请求同时 401 时只刷新一次 */
let refreshing: Promise<string | null> | null = null

async function refreshAccessToken(): Promise<string | null> {
  const refreshToken = localStorage.getItem(REFRESH_KEY)
  if (!refreshToken) return null
  try {
    // 用裸 axios，绕开本拦截器，避免刷新失败时递归
    const { data } = await axios.post<ApiResponse<LoginVO>>('/api/v1/auth/refresh', { refreshToken })
    if (data.code !== 0) return null
    localStorage.setItem(TOKEN_KEY, data.data.accessToken)
    localStorage.setItem(REFRESH_KEY, data.data.refreshToken)
    return data.data.accessToken
  } catch {
    return null
  }
}

function clearAuthAndRedirect() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(REFRESH_KEY)
  if (location.pathname !== '/login') {
    const redirect = encodeURIComponent(location.pathname + location.search)
    location.href = `/login?redirect=${redirect}`
  }
}

http.interceptors.response.use(
  (res) => res,
  async (error: AxiosError<ApiResponse<unknown>>) => {
    const status = error.response?.status
    const config = error.config as (InternalAxiosRequestConfig & { _retry?: boolean }) | undefined
    const url = config?.url ?? ''

    // access token 过期 → 尝试用 refresh token 换一次，然后重放原请求
    if (status === 401 && config && !config._retry && !url.includes('/auth/refresh')) {
      config._retry = true
      if (!refreshing) {
        refreshing = refreshAccessToken().finally(() => {
          refreshing = null
        })
      }
      const newToken = await refreshing
      if (newToken) {
        config.headers.Authorization = `Bearer ${newToken}`
        return http(config)
      }
    }

    if (status === 401) {
      clearAuthAndRedirect()
      return Promise.reject(new Error('未登录'))
    }

    showToast(error.response?.data?.message || '网络异常，请稍后再试')
    return Promise.reject(error)
  }
)

function unwrap<T>(res: ApiResponse<T>): T {
  if (res.code !== 0) {
    showToast(res.message || '请求失败')
    throw new Error(res.message)
  }
  return res.data
}

export async function get<T>(url: string, params?: Record<string, unknown>): Promise<T> {
  const { data } = await http.get<ApiResponse<T>>(url, { params })
  return unwrap(data)
}

export async function post<T>(url: string, body?: unknown): Promise<T> {
  const { data } = await http.post<ApiResponse<T>>(url, body)
  return unwrap(data)
}
