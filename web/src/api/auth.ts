import { get, post } from './request'
import type { LoginVO, UserVO } from './types'

export function apiLogin(phone: string, password: string): Promise<LoginVO> {
  return post<LoginVO>('/auth/login', { phone, password })
}

export function apiRegister(phone: string, password: string, nickname?: string): Promise<LoginVO> {
  return post<LoginVO>('/auth/register', { phone, password, nickname })
}

export function apiLogout(refreshToken: string): Promise<void> {
  return post<void>('/auth/logout', { refreshToken })
}

export function fetchMe(): Promise<UserVO> {
  return get<UserVO>('/auth/me')
}
