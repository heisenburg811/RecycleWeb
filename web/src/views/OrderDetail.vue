<template>
  <div class="page">
    <van-nav-bar title="订单详情" left-arrow @click-left="router.back()" />

    <template v-if="order">
      <div class="hero" :class="order.status.toLowerCase()">
        <div class="hero-status">{{ order.statusLabel }}</div>
        <div v-if="order.status === 'PENDING_PAYMENT'" class="hero-sub">
          请在 {{ countdown }} 内完成支付
        </div>
        <div v-else-if="order.status === 'PAID'" class="hero-sub">等待卖家发货</div>
        <div v-else-if="order.status === 'SHIPPED'" class="hero-sub">确认无误后请点确认收货</div>
        <div v-else-if="order.status === 'COMPLETED'" class="hero-sub">交易完成，感谢让旧物新生</div>
        <div v-else class="hero-sub">交易已结束</div>
      </div>

      <van-cell-group inset title="商品">
        <van-cell :title="order.itemTitle" :label="`订单号 ${order.orderNo}`" :icon="order.itemCover || undefined" is-link :to="`/item/${order.itemId}`" />
      </van-cell-group>

      <van-cell-group inset :title="isSeller ? '货款结算' : '费用'">
        <template v-if="isSeller">
          <van-cell title="商品成交价" :value="`¥${order.itemPrice}`" />
          <van-cell title="平台服务费" :value="`-¥${order.serviceFee}`" value-class="minus" />
          <van-cell title="你实收" :value="`¥${order.sellerIncome}`" class="total" />
          <div class="note">运费与搬运费由买家支付给运力方，不经过你</div>
        </template>
        <template v-else>
          <van-cell title="商品价" :value="`¥${order.itemPrice}`" />
          <van-cell v-if="order.deliveryType === 2" title="运费" :value="`¥${order.deliveryFee}`" />
          <van-cell v-if="Number(order.floorFee) > 0" title="楼层搬运费" :value="`¥${order.floorFee}`" />
          <van-cell v-if="Number(order.assemblyFee) > 0" title="拆装费" :value="`¥${order.assemblyFee}`" />
          <van-cell title="实付" :value="`¥${order.totalAmount}`" class="total" />
        </template>
      </van-cell-group>

      <van-cell-group inset title="信息">
        <van-cell title="配送方式" :value="order.deliveryType === 2 ? '平台配送' : '同城自提'" />
        <van-cell v-if="order.addressSnapshot" title="地址" :value="order.addressSnapshot" />
        <van-cell title="下单时间" :value="format(order.createdAt)" />
        <van-cell v-if="order.paidAt" title="支付时间" :value="format(order.paidAt)" />
        <van-cell v-if="order.completedAt" title="完成时间" :value="format(order.completedAt)" />
      </van-cell-group>

      <div class="actions">
        <template v-if="isBuyer && order.status === 'PENDING_PAYMENT'">
          <van-button block round plain @click="onCancel">取消订单</van-button>
          <van-button block round type="danger" class="mt" :loading="busy" @click="onPay">立即支付</van-button>
        </template>
        <van-button v-if="isSeller && order.status === 'PAID'" block round type="primary" :loading="busy" @click="onShip">
          发货
        </van-button>
        <van-button v-if="isBuyer && order.status === 'SHIPPED'" block round type="danger" :loading="busy" @click="onConfirm">
          确认收货
        </van-button>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import { cancelOrder, confirmOrder, getOrder, payOrder, shipOrder } from '@/api/order'
import type { OrderVO } from '@/api/types'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const store = useUserStore()
const order = ref<OrderVO | null>(null)
const busy = ref(false)
const now = ref(Date.now())
let timer: number | undefined

const isBuyer = computed(() => store.user?.id === order.value?.buyerId)
const isSeller = computed(() => store.user?.id === order.value?.sellerId)

const countdown = computed(() => {
  if (!order.value?.payExpireAt) return '-'
  const left = new Date(order.value.payExpireAt).getTime() - now.value
  if (left <= 0) return '00:00'
  const m = Math.floor(left / 60000)
  const s = Math.floor((left % 60000) / 1000)
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
})

function format(v: string | null) {
  return v ? v.replace('T', ' ').slice(0, 16) : ''
}

async function load() {
  order.value = await getOrder(String(route.params.orderNo))
}

async function run(fn: () => Promise<OrderVO>, tip: string) {
  busy.value = true
  try {
    order.value = await fn()
    showToast(tip)
  } catch {
    // 请求层已提示
  } finally {
    busy.value = false
  }
}

const onPay = () => run(() => payOrder(order.value!.orderNo), '支付成功')
const onShip = () => run(() => shipOrder(order.value!.orderNo), '已发货')
const onConfirm = () => run(() => confirmOrder(order.value!.orderNo), '交易完成')
const onCancel = () => run(() => cancelOrder(order.value!.orderNo), '订单已取消')

onMounted(async () => {
  if (!store.user) {
    await store.loadMe().catch(() => undefined)
  }
  await load()
  timer = window.setInterval(() => (now.value = Date.now()), 1000)
})

onUnmounted(() => {
  if (timer) window.clearInterval(timer)
})
</script>

<style scoped>
.hero {
  padding: 20px 16px;
  color: #fff;
  background: #969799;
}

.hero.pending_payment {
  background: linear-gradient(135deg, #ff6034, #ee0a24);
}

.hero.paid,
.hero.shipped {
  background: linear-gradient(135deg, #1989fa, #0570db);
}

.hero.completed {
  background: linear-gradient(135deg, #07c160, #059e4c);
}

.hero-status {
  font-size: 20px;
  font-weight: 600;
}

.hero-sub {
  font-size: 13px;
  opacity: 0.9;
  margin-top: 6px;
}

.total {
  font-weight: 600;
}

.note {
  font-size: 12px;
  color: #969799;
  padding: 8px 16px;
}

:deep(.minus) {
  color: #ee0a24;
}

.actions {
  padding: 16px;
}

.mt {
  margin-top: 10px;
}
</style>
