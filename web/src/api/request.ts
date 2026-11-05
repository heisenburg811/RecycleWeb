import axios from 'axios'
import { showToast } from 'vant'

interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export const TOKEN_KEY = 'recycle_token'

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

http.interceptors.response.use(
  (res) => res,
  (error) => {
    const status = error.response?.status
    const message = error.response?.data?.message

    if (status === 401) {
      // token 过期或未登录：清掉本地凭证，带 redirect 回登录页
      localStorage.removeItem(TOKEN_KEY)
      if (location.pathname !== '/login') {
        const redirect = encodeURIComponent(location.pathname + location.search)
        location.href = `/login?redirect=${redirect}`
      }
      return Promise.reject(new Error('未登录'))
    }

    showToast(message || '网络异常，请稍后再试')
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
