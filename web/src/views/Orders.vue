<template>
  <div class="page">
    <van-nav-bar title="我的订单" />

    <van-tabs v-model:active="role">
      <van-tab title="我买到的" name="buyer" />
      <van-tab title="我卖出的" name="seller" />
    </van-tabs>

    <van-empty
      v-if="!loading && list.length === 0"
      :description="role === 'buyer' ? '还没有买过东西' : '还没有卖出过东西'"
    />

    <div v-for="o in list" :key="o.orderNo" class="order" @click="open(o.orderNo)">
      <div class="head">
        <span class="no">{{ o.orderNo }}</span>
        <span class="status" :class="o.status.toLowerCase()">{{ o.statusLabel }}</span>
      </div>
      <div class="body">
        <img :src="o.itemCover || ''" class="cover" alt="" />
        <div class="info">
          <div class="title">{{ o.itemTitle }}</div>
          <div class="meta">
            {{ o.deliveryType === 2 ? '平台配送' : '同城自提' }} · {{ format(o.createdAt) }}
          </div>
        </div>
        <div class="price">
          <span class="price-tag">{{ role === 'seller' ? '实收' : '实付' }}</span>
          <span class="price-num">¥{{ role === 'seller' ? o.sellerIncome : o.totalAmount }}</span>
        </div>
      </div>
    </div>

    <van-button v-if="hasMore" block plain class="more" @click="load()">加载更多</van-button>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { pageOrders } from '@/api/order'
import type { OrderVO } from '@/api/types'

const router = useRouter()
const route = useRoute()

type Role = 'buyer' | 'seller'

/** 直接用语义化的 name，不再用下标推算角色——下标和角色一旦错位就会串数据 */
const role = ref<Role>(route.query.role === 'seller' ? 'seller' : 'buyer')
const list = ref<OrderVO[]>([])
const page = ref(1)
const hasMore = ref(false)
const loading = ref(false)

/** 请求序号：切 Tab 时丢弃旧请求的响应，避免慢请求覆盖新列表 */
let seq = 0

function format(v: string) {
  return v ? v.replace('T', ' ').slice(0, 16) : ''
}

async function load(reset = false) {
  const mySeq = ++seq
  if (reset) {
    page.value = 1
    hasMore.value = false
  }
  loading.value = true
  try {
    const res = await pageOrders({ role: role.value, page: page.value, size: 10 })
    if (mySeq !== seq) return
    list.value = reset ? res.list : [...list.value, ...res.list]
    page.value += 1
    hasMore.value = list.value.length < res.total
  } catch {
    if (mySeq === seq) hasMore.value = false
  } finally {
    if (mySeq === seq) loading.value = false
  }
}

// 用 watch 而不是 tabs 的 change 事件：保证「角色已确定」之后再发请求
watch(role, (value) => {
  load(true)
  const query = value === 'seller' ? { role: 'seller' } : {}
  if (route.query.role !== query.role) {
    router.replace({ path: '/orders', query })
  }
})

// 从「我的」页带 ?role=seller 进来、或组件被复用时同步角色
watch(
  () => route.query.role,
  (value) => {
    role.value = value === 'seller' ? 'seller' : 'buyer'
  }
)

function open(orderNo: string) {
  router.push(`/order/${orderNo}`)
}

onMounted(() => load(true))
</script>

<style scoped>
.order {
  background: #fff;
  margin: 10px 12px;
  border-radius: 8px;
  padding: 12px;
}

.head {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #969799;
  padding-bottom: 10px;
  border-bottom: 1px solid #f2f3f5;
}

.status {
  font-weight: 500;
  color: #646566;
}

.status.pending_payment {
  color: #ee0a24;
}

.status.paid,
.status.shipped {
  color: #1989fa;
}

.status.completed {
  color: #07c160;
}

.body {
  display: flex;
  gap: 10px;
  padding-top: 10px;
  align-items: center;
}

.cover {
  width: 64px;
  height: 64px;
  border-radius: 6px;
  object-fit: cover;
  background: #f2f3f5;
  flex-shrink: 0;
}

.info {
  flex: 1;
  min-width: 0;
}

.title {
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.meta {
  font-size: 12px;
  color: #969799;
  margin-top: 6px;
}

.price {
  flex-shrink: 0;
  text-align: right;
}

.price-tag {
  display: block;
  font-size: 11px;
  color: #969799;
}

.price-num {
  color: #ee0a24;
  font-weight: 600;
}

.more {
  margin: 12px 0;
}
</style>
