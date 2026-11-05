import { get, post } from './request'
import type { FeeDetail, OrderVO, PageResult } from './types'

export function quoteOrder(payload: Record<string, unknown>): Promise<FeeDetail> {
  return post<FeeDetail>('/orders/quote', payload)
}

export function createOrder(payload: Record<string, unknown>): Promise<OrderVO> {
  return post<OrderVO>('/orders', payload)
}

export function payOrder(orderNo: string, channel = 'mock'): Promise<OrderVO> {
  return post<OrderVO>(`/orders/${orderNo}/pay`, { channel })
}

export function confirmOrder(orderNo: string): Promise<OrderVO> {
  return post<OrderVO>(`/orders/${orderNo}/confirm`)
}

export function cancelOrder(orderNo: string): Promise<OrderVO> {
  return post<OrderVO>(`/orders/${orderNo}/cancel`)
}

export function getOrder(orderNo: string): Promise<OrderVO> {
  return get<OrderVO>(`/orders/${orderNo}`)
}

export function pageOrders(params: {
  role: 'buyer' | 'seller'
  page?: number
  size?: number
}): Promise<PageResult<OrderVO>> {
  return get<PageResult<OrderVO>>('/orders', params as Record<string, unknown>)
}
