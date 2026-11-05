export interface SlotVO {
  code: string
  label: string
  capacity: number
  booked: number
  remaining: number
  available: boolean
}

export interface LogisticsVO {
  orderNo: string
  scheduleDate: string
  scheduleSlot: string
  provider: string
  driverName: string
  driverPhone: string
  status: string
  rescheduleCount: number
  canReschedule: boolean
}
