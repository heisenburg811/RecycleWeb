<template>
  <div v-if="item" class="page">
    <van-swipe :autoplay="0" indicator-color="#1989fa">
      <van-swipe-item v-for="(img, i) in item.images" :key="i">
        <img :src="img" class="banner" alt="" />
      </van-swipe-item>
    </van-swipe>

    <div class="card">
      <div class="price">{{ item.price }}</div>
      <div class="title">{{ item.title }}</div>
      <div class="tags">
        <van-tag type="primary" plain>{{ item.conditionLabel }}</van-tag>
        <van-tag plain>{{ tradeText }}</van-tag>
        <van-tag plain>{{ item.districtCode }}</van-tag>
      </div>
    </div>

    <div class="card">
      <div class="label">规格</div>
      <div class="specs">
        <span v-if="item.lengthCm">长 {{ item.lengthCm }}cm</span>
        <span v-if="item.widthCm">宽 {{ item.widthCm }}cm</span>
        <span v-if="item.heightCm">高 {{ item.heightCm }}cm</span>
        <span v-if="item.material">{{ item.material }}</span>
        <span v-if="item.style">{{ item.style }}</span>
        <span v-if="item.brand">{{ item.brand }}</span>
      </div>
    </div>

    <div class="card">
      <div class="label">描述</div>
      <p class="desc">{{ item.description || '卖家未填写描述' }}</p>
    </div>

    <div class="bar">
      <div class="bar-price">
        <span class="price">{{ item.price }}</span>
        <span class="tiny">不含运费</span>
      </div>
      <van-button type="danger" round @click="goOrder">立即购买</van-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getItem } from '@/api/item'
import type { ItemDetail } from '@/api/types'

const route = useRoute()
const router = useRouter()
const item = ref<ItemDetail | null>(null)

const tradeText = computed(() => {
  if (!item.value) return ''
  return item.value.tradeType === 1 ? '仅自提' : item.value.tradeType === 2 ? '仅配送' : '自提/配送均可'
})

onMounted(async () => {
  item.value = await getItem(String(route.params.id))
})

function goOrder() {
  router.push(`/order/confirm/${route.params.id}`)
}
</script>

<style scoped>
.banner {
  width: 100%;
  height: 300px;
  object-fit: cover;
  background: #f2f3f5;
}

.title {
  font-size: 16px;
  font-weight: 500;
  margin: 8px 0;
}

.tags {
  display: flex;
  gap: 6px;
}

.label {
  font-size: 13px;
  color: #969799;
  margin-bottom: 6px;
}

.specs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  font-size: 13px;
  color: #646566;
}

.desc {
  font-size: 14px;
  line-height: 1.6;
  margin: 0;
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

.bar-price {
  display: flex;
  flex-direction: column;
}

.tiny {
  font-size: 11px;
  color: #969799;
}
</style>
