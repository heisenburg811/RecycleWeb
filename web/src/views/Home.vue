<template>
  <div class="page">
    <van-search v-model="keyword" placeholder="搜沙发、床、餐桌" @search="goSearch" />

    <div class="cats">
      <div v-for="c in categories" :key="c.id" class="cat" @click="goCategory(c.id)">
        <span class="cat-icon">{{ c.name.charAt(0) }}</span>
        <span class="cat-name">{{ c.name }}</span>
      </div>
    </div>

    <van-list
      v-model:loading="loading"
      :finished="finished"
      finished-text="没有更多了"
      @load="onLoad"
    >
      <div v-for="it in items" :key="it.id" class="item" @click="goDetail(it.id)">
        <img :src="it.coverUrl" class="cover" alt="" />
        <div class="info">
          <div class="title">{{ it.title }}</div>
          <div class="meta">{{ it.conditionLabel }} · {{ it.viewCount }} 次浏览</div>
          <div class="price">{{ it.price }}</div>
        </div>
      </div>
    </van-list>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { getCategories, pageItems } from '@/api/item'
import type { CategoryNode, ItemCard } from '@/api/types'

const router = useRouter()
const keyword = ref('')
const categories = ref<CategoryNode[]>([])
const items = ref<ItemCard[]>([])
const loading = ref(false)
const finished = ref(false)
const page = ref(1)

getCategories()
  .then((data) => (categories.value = data))
  .catch(() => undefined)

async function onLoad() {
  try {
    const res = await pageItems({ page: page.value, size: 10 })
    items.value.push(...res.list)
    page.value += 1
    finished.value = res.list.length === 0 || items.value.length >= res.total
  } catch {
    finished.value = true
  } finally {
    loading.value = false
  }
}

function goSearch() {
  router.push({ path: '/search', query: { keyword: keyword.value } })
}

function goCategory(id: number) {
  router.push({ path: '/search', query: { categoryId: String(id) } })
}

function goDetail(id: number) {
  router.push(`/item/${id}`)
}
</script>

<style scoped>
.cats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
  background: #fff;
  padding: 12px;
  margin-bottom: 8px;
}

.cat {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.cat-icon {
  width: 44px;
  height: 44px;
  line-height: 44px;
  text-align: center;
  border-radius: 50%;
  background: #eef5ff;
  color: #1989fa;
  font-size: 18px;
}

.cat-name {
  font-size: 12px;
  color: #646566;
}

.item {
  display: flex;
  gap: 12px;
  background: #fff;
  padding: 12px;
  border-bottom: 1px solid #f2f3f5;
}

.cover {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 6px;
  background: #f2f3f5;
}

.info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.title {
  font-size: 14px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.meta {
  font-size: 12px;
  color: #969799;
}
</style>
