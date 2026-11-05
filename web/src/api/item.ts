import { get, post } from './request'
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

export function getCategories(): Promise<CategoryNode[]> {
  return get<CategoryNode[]>('/categories')
}

export function getHotWords(): Promise<string[]> {
  return get<string[]>('/search/hot')
}
