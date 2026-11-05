<template>
  <div class="page">
    <van-nav-bar title="我的发布" left-arrow @click-left="router.back()" />

    <van-tabs v-model:active="tab">
      <van-tab v-for="t in tabs" :key="t.name" :title="t.title" :name="t.name" />
    </van-tabs>

    <van-empty v-if="!loading && list.length === 0" description="这里还没有商品，去发布一件吧" />

    <div v-for="it in list" :key="it.id" class="card">
      <div class="row" @click="openItem(it.id)">
        <img :src="it.coverUrl" class="cover" alt="" />
        <div class="info">
          <div class="title">{{ it.title }}</div>
          <div class="meta">
            <van-tag :type="tagType(it.status)" plain>{{ statusLabel(it.status) }}</van-tag>
            <span class="views">{{ it.viewCount }} 次浏览</span>
          </div>
          <div class="price">¥{{ it.price }}</div>
        </div>
      </div>

      <div class="actions">
        <van-button
          v-if="canEdit(it.status)"
          size="small"
          plain
          @click="edit(it.id)"
        >编辑</van-button>
        <van-button
          v-if="it.status === 'ON_SALE'"
          size="small"
          plain
          @click="toggle(it, 'OFF_SHELF')"
        >下架</van-button>
        <van-button
          v-if="it.status === 'OFF_SHELF'"
          size="small"
          plain
          type="primary"
          @click="toggle(it, 'ON_SALE')"
        >重新上架</van-button>
        <van-button
          v-if="canEdit(it.status)"
          size="small"
          plain
          type="danger"
          @click="remove(it)"
        >删除</van-button>
      </div>
    </div>

    <van-button v-if="hasMore" block plain class="more" @click="load()">加载更多</van-button>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { showConfirmDialog, showToast } from 'vant'
import { changeItemStatus, deleteItem, myItems } from '@/api/item'
import type { ItemCard } from '@/api/types'

const router = useRouter()

const tabs = [
  { title: '全部', name: 'ALL' },
  { title: '在售', name: 'ON_SALE' },
  { title: '已下架', name: 'OFF_SHELF' },
  { title: '已售出', name: 'SOLD' }
]

const tab = ref('ALL')
const list = ref<ItemCard[]>([])
const page = ref(1)
const hasMore = ref(false)
const loading = ref(false)

/** 请求序号：切 Tab 时丢弃过期响应 */
let seq = 0

const LABELS: Record<string, string> = {
  ON_SALE: '在售',
  OFF_SHELF: '已下架',
  SOLD: '已售出',
  RESERVED: '已预订',
  PENDING: '审核中',
  REJECTED: '已驳回',
  BANNED: '违规下架',
  DRAFT: '草稿'
}

function statusLabel(s: string) {
  return LABELS[s] ?? s
}

function tagType(s: string) {
  if (s === 'ON_SALE') return 'success'
  if (s === 'SOLD' || s === 'RESERVED') return 'primary'
  if (s === 'BANNED') return 'danger'
  return 'default'
}

/** 已预订 / 已售出的商品不允许编辑和删除，和后端规则保持一致 */
function canEdit(status: string) {
  return status !== 'RESERVED' && status !== 'SOLD'
}

async function load(reset = false) {
  const mySeq = ++seq
  if (reset) {
    page.value = 1
    hasMore.value = false
  }
  loading.value = true
  try {
    const res = await myItems({
      status: tab.value === 'ALL' ? undefined : tab.value,
      page: page.value,
      size: 10
    })
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

watch(tab, () => load(true))

function openItem(id: number) {
  router.push(`/item/${id}`)
}

function edit(id: number) {
  router.push({ path: '/publish', query: { id: String(id) } })
}

async function toggle(item: ItemCard, status: 'ON_SALE' | 'OFF_SHELF') {
  try {
    await changeItemStatus(item.id, status)
    showToast(status === 'ON_SALE' ? '已上架' : '已下架')
    await load(true)
  } catch {
    // 请求层已提示
  }
}

async function remove(item: ItemCard) {
  try {
    await showConfirmDialog({ title: '删除商品', message: `确定删除「${item.title}」吗？删除后不可恢复。` })
  } catch {
    return
  }
  try {
    await deleteItem(item.id)
    showToast('已删除')
    await load(true)
  } catch {
    // 请求层已提示
  }
}

onMounted(() => load(true))
</script>

<style scoped>
.card {
  background: #fff;
  margin: 10px 12px;
  border-radius: 8px;
  padding: 12px;
}

.row {
  display: flex;
  gap: 10px;
}

.cover {
  width: 80px;
  height: 80px;
  border-radius: 6px;
  object-fit: cover;
  background: #f2f3f5;
  flex-shrink: 0;
}

.info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.title {
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #969799;
}

.price {
  color: #ee0a24;
  font-weight: 600;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #f2f3f5;
}

.more {
  margin: 12px 0;
}
</style>
