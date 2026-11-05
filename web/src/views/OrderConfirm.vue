<template>
  <div class="page">
    <van-nav-bar title="确认订单" left-arrow @click-left="router.back()" />

    <van-cell-group inset title="商品">
      <van-cell :title="item?.title" :label="item?.conditionLabel" />
    </van-cell-group>

    <van-cell-group inset title="配送方式">
      <van-radio-group v-model="form.deliveryType">
        <van-cell title="平台配送" label="可预约时段，按体积与楼层计价" clickable @click="form.deliveryType = 2">
          <template #right-icon><van-radio :name="2" /></template>
        </van-cell>
        <van-cell title="同城自提" label="免运费，与卖家约定时间" clickable @click="form.deliveryType = 1">
          <template #right-icon><van-radio :name="1" /></template>
        </van-cell>
      </van-radio-group>
    </van-cell-group>

    <van-cell-group v-if="form.deliveryType === 2" inset title="配送信息">
      <van-field v-model="form.address" label="收货地址" placeholder="小区 + 楼栋 + 门牌" maxlength="200" />
      <van-field v-model="form.distanceKm" type="number" label="距离(km)" placeholder="如 5.2" />
      <van-field v-model="form.floor" type="number" label="楼层" placeholder="1 表示一楼" />
      <van-field label="有电梯">
        <template #input><van-switch v-model="form.hasElevator" size="20px" /></template>
      </van-field>
      <van-field label="需要拆装">
        <template #input>
          <van-radio-group v-model="form.assemblyType" direction="horizontal">
            <van-radio name="none">不需要</van-radio>
            <van-radio name="bed">床</van-radio>
            <van-radio name="wardrobe">衣柜</van-radio>
            <van-radio name="sofa">沙发</van-radio>
          </van-radio-group>
        </template>
      </van-field>
    </van-cell-group>

    <van-cell-group inset title="费用明细">
      <van-cell title="商品价" :value="`¥${fee?.itemPrice ?? '-'}`" />
      <van-cell v-if="form.deliveryType === 2" title="运费" :value="`¥${fee?.deliveryFee ?? '-'}`" />
      <van-cell v-if="Number(fee?.floorFee) > 0" title="楼层搬运费" :value="`¥${fee?.floorFee ?? '-'}`" />
      <van-cell v-if="Number(fee?.assemblyFee) > 0" title="拆装费" :value="`¥${fee?.assemblyFee ?? '-'}`" />
      <van-cell title="应付总额" :value="`¥${fee?.totalAmount ?? '-'}`" class="total" />
      <div class="note">
        {{ fee?.note }}。平台服务费 ¥{{ fee?.serviceFee ?? 0 }} 由卖家承担，不额外向你收取。
      </div>
    </van-cell-group>

    <div class="bar">
      <div class="total-text">合计 <span class="price">{{ fee?.totalAmount ?? '-' }}</span></div>
      <van-button type="danger" round :loading="submitting" @click="submit">提交订单</van-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import { getItem } from '@/api/item'
import { createOrder, quoteOrder } from '@/api/order'
import type { FeeDetail, ItemDetail } from '@/api/types'

const route = useRoute()
const router = useRouter()
const item = ref<ItemDetail | null>(null)
const fee = ref<FeeDetail | null>(null)
const submitting = ref(false)

const form = reactive({
  deliveryType: 2,
  distanceKm: '5',
  floor: '2',
  hasElevator: false,
  assemblyType: 'none',
  address: ''
})

async function refreshQuote() {
  try {
    fee.value = await quoteOrder({
      itemId: Number(route.params.id),
      deliveryType: form.deliveryType,
      distanceKm: form.deliveryType === 2 ? Number(form.distanceKm) : null,
      floor: form.deliveryType === 2 ? Number(form.floor) : null,
      hasElevator: form.hasElevator,
      assemblyType: form.assemblyType
    })
  } catch {
    // 请求层已提示
  }
}

onMounted(async () => {
  item.value = await getItem(String(route.params.id))
  // 商品只支持自提时，默认切过去
  if (item.value?.tradeType === 1) form.deliveryType = 1
  await refreshQuote()
})

watch(
  () => [form.deliveryType, form.distanceKm, form.floor, form.hasElevator, form.assemblyType],
  refreshQuote,
  { deep: true }
)

async function submit() {
  if (form.deliveryType === 2 && !form.address.trim()) {
    showToast('请填写收货地址')
    return
  }
  submitting.value = true
  try {
    const order = await createOrder({
      itemId: Number(route.params.id),
      deliveryType: form.deliveryType,
      distanceKm: form.deliveryType === 2 ? Number(form.distanceKm) : null,
      floor: form.deliveryType === 2 ? Number(form.floor) : null,
      hasElevator: form.hasElevator,
      assemblyType: form.assemblyType,
      address: form.deliveryType === 2 ? form.address : null
    })
    showToast('下单成功，请在 30 分钟内支付')
    router.replace(`/order/${order.orderNo}`)
  } catch {
    // 请求层已提示
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.total {
  font-weight: 600;
}

.note {
  font-size: 12px;
  color: #969799;
  padding: 8px 16px;
}

.bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 50px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 16px;
  background: #fff;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.06);
}

.total-text {
  font-size: 14px;
}
</style>
