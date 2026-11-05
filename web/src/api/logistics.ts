import { get, post } from './request'
import type { LogisticsVO, SlotVO } from './logistics-types'

export function getSlots(date: string): Promise<SlotVO[]> {
  return get<SlotVO[]>('/logistics/slots', { date })
}

export function getLogistics(orderNo: string): Promise<LogisticsVO | null> {
  return get<LogisticsVO | null>(`/orders/${orderNo}/logistics`)
}

export function bookLogistics(orderNo: string, date: string, slotCode: string): Promise<LogisticsVO> {
  return post<LogisticsVO>(`/orders/${orderNo}/logistics`, { date, slotCode })
}

export function rescheduleLogistics(
  orderNo: string,
  date: string,
  slotCode: string
): Promise<LogisticsVO> {
  return post<LogisticsVO>(`/orders/${orderNo}/logistics/reschedule`, { date, slotCode })
}
