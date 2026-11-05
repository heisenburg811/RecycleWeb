import { del, get, patch, post, put } from './request'
import type { CategoryNode, ItemCard, ItemDetail, PageQuery, PageResult } from './types'

export function pageItems(query: PageQuery): Promise<PageResult<ItemCard>> {
  return get<PageResult<ItemCard>>('/items', query as Record<string, unknown>)
}

export function getItem(id: number | string): Promise<ItemDetail> {
  return get<ItemDetail>(`/items/${id}`)
}

export function publishItem(payload: Record<string, unknown>): Promise<number> {
  return post<number>('/items', payload)
}

export function updateItem(id: number, payload: Record<string, unknown>): Promise<void> {
  return put<void>(`/items/${id}`, payload)
}

export function myItems(params: {
  status?: string
  page?: number
  size?: number
}): Promise<PageResult<ItemCard>> {
  return get<PageResult<ItemCard>>('/items/mine', params as Record<string, unknown>)
}

export function changeItemStatus(id: number, status: 'ON_SALE' | 'OFF_SHELF'): Promise<void> {
  return patch<void>(`/items/${id}/status?status=${status}`)
}

export function deleteItem(id: number): Promise<void> {
  return del<void>(`/items/${id}`)
}

export function uploadImage(file: File): Promise<string> {
  const form = new FormData()
  form.append('file', file)
  return post<string>('/files', form)
}

export function getCategories(): Promise<CategoryNode[]> {
  return get<CategoryNode[]>('/categories')
}

export function getHotWords(): Promise<string[]> {
  return get<string[]>('/search/hot')
}
