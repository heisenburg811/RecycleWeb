<template>
  <div class="page">
    <van-nav-bar title="我的订单" />

    <van-tabs v-model:active="roleIndex" @change="reload">
      <van-tab title="我买到的" />
      <van-tab title="我卖出的" />
    </van-tabs>

    <van-empty v-if="!loading && list.length === 0" :description="roleIndex === 0 ? '还没有买过东西' : '还没有卖出过东西'" />

    <div v-for="o in list" :key="o.orderNo" class="order" @click="open(o.orderNo)">
      <div class="head">
        <span class="no">{{ o.orderNo }}</span>
        <span class="status" :class="o.status.toLowerCase()">{{ o.statusLabel }}</span>
      </div>
      <div class="body">
        <img :src="o.itemCover || ''" class="cover" alt="" />
        <div class="info">
          <div class="title">{{ o.itemTitle }}</div>
          <div class="meta">{{ o.deliveryType === 2 ? '平台配送' : '同城自提' }} · {{ format(o.createdAt) }}</div>
        </div>
        <div class="price">{{ o.totalAmount }}</div>
      </div>
    </div>

    <van-button v-if="hasMore" block plain class="more" @click="load">加载更多</van-button>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { pageOrders } from '@/api/order'
import type { OrderVO } from '@/api/types'

const router = useRouter()
const route = useRoute()

const roleIndex = ref(route.query.role === 'seller' ? 1 : 0)
const list = ref<OrderVO[]>([])
const page = ref(1)
const hasMore = ref(false)
const loading = ref(false)

function format(v: string) {
  return v ? v.replace('T', ' ').slice(0, 16) : ''
}

async function load(reset = false) {
  if (loading.value) return
  loading.value = true
  if (reset) {
    page.value = 1
    list.value = []
  }
  try {
    const res = await pageOrders({
      role: roleIndex.value === 0 ? 'buyer' : 'seller',
      page: page.value,
      size: 10
    })
    list.value.push(...res.list)
    hasMore.value = list.value.length < res.total
    page.value += 1
  } catch {
    hasMore.value = false
  } finally {
    loading.value = false
  }
}

function reload() {
  load(true)
}

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
  color: #ee0a24;
  font-weight: 600;
  flex-shrink: 0;
}

.price::before {
  content: '¥';
  font-size: 12px;
}

.more {
  margin: 12px 0;
}
</style>
