export interface ItemCard {
  id: number
  title: string
  price: number
  coverUrl: string
  itemCondition: number
  conditionLabel: string
  districtCode: string
  tradeType: number
  viewCount: number
  createdAt: string
}

export interface ItemDetail {
  id: number
  sellerId: number
  categoryId: number
  title: string
  description: string
  price: number
  itemCondition: number
  conditionLabel: string
  lengthCm: number | null
  widthCm: number | null
  heightCm: number | null
  material: string | null
  style: string | null
  brand: string | null
  tradeType: number
  districtCode: string
  status: string
  images: string[]
  viewCount: number
  createdAt: string
}

export interface PageResult<T> {
  list: T[]
  total: number
  page: number
  size: number
}

export interface CategoryNode {
  id: number
  name: string
  icon: string | null
  children: CategoryNode[]
}

export interface FeeDetail {
  itemId: number
  itemPrice: number
  deliveryFee: number
  floorFee: number
  assemblyFee: number
  serviceFee: number
  totalAmount: number
  note: string
}

export interface PageQuery {
  keyword?: string
  categoryId?: number
  minPrice?: number
  maxPrice?: number
  conditions?: number[]
  tradeType?: number
  sort?: string
  page?: number
  size?: number
}
