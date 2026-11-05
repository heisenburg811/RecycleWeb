<template>
  <div class="page">
    <van-search v-model="keyword" placeholder="搜沙发、床、餐桌" @search="reload" />

    <van-dropdown-menu>
      <van-dropdown-item v-model="sort" :options="sortOptions" @change="reload" />
    </van-dropdown-menu>

    <div v-if="items.length === 0 && !loading" class="empty">
      <van-empty description="没有找到相关商品，换个关键词试试" />
    </div>

    <div v-for="it in items" :key="it.id" class="item" @click="goDetail(it.id)">
      <img :src="it.coverUrl" class="cover" alt="" />
      <div class="info">
        <div class="title">{{ it.title }}</div>
        <div class="meta">{{ it.conditionLabel }} · {{ it.viewCount }} 次浏览</div>
        <div class="price">{{ it.price }}</div>
      </div>
    </div>

    <van-button v-if="items.length > 0" block plain class="more" @click="loadMore">
      加载更多
    </van-button>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { pageItems } from '@/api/item'
import type { ItemCard } from '@/api/types'

const route = useRoute()
const router = useRouter()

const keyword = ref(String(route.query.keyword ?? ''))
const categoryId = ref<number | undefined>(
  route.query.categoryId ? Number(route.query.categoryId) : undefined
)
const sort = ref('composite')
const items = ref<ItemCard[]>([])
const loading = ref(false)
const page = ref(1)
const finished = ref(false)

const sortOptions = [
  { text: '综合排序', value: 'composite' },
  { text: '最新发布', value: 'latest' },
  { text: '价格从低到高', value: 'price_asc' },
  { text: '价格从高到低', value: 'price_desc' }
]

async function load(reset = false) {
  if (loading.value) return
  loading.value = true
  if (reset) {
    page.value = 1
    finished.value = false
    items.value = []
  }
  try {
    const res = await pageItems({
      keyword: keyword.value || undefined,
      categoryId: categoryId.value,
      sort: sort.value,
      page: page.value,
      size: 10
    })
    items.value.push(...res.list)
    finished.value = res.list.length === 0 || items.value.length >= res.total
    page.value += 1
  } finally {
    loading.value = false
  }
}

function reload() {
  load(true)
}

function loadMore() {
  if (!finished.value) load()
}

function goDetail(id: number) {
  router.push(`/item/${id}`)
}

onMounted(() => load(true))
</script>

<style scoped>
.empty {
  padding-top: 40px;
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
}

.meta {
  font-size: 12px;
  color: #969799;
}

.more {
  margin: 12px 0;
}
</style>
