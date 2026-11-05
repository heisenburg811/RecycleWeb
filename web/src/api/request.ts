import axios from 'axios'
import { showToast } from 'vant'

interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

const http = axios.create({
  baseURL: '/api/v1',
  timeout: 15000
})

http.interceptors.request.use((config) => {
  // TODO 接入登录后从 store 取 token
  config.headers['X-User-Id'] = '1'
  return config
})

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
