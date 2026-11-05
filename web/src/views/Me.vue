<template>
  <div class="page">
    <van-nav-bar title="我的" />

    <template v-if="store.isLogin">
      <div class="card user">
        <van-image round width="56" height="56" :src="store.user?.avatar || defaultAvatar" />
        <div class="info">
          <div class="nick">{{ store.user?.nickname || '加载中…' }}</div>
          <div class="phone">{{ maskedPhone }}</div>
        </div>
      </div>

      <van-cell-group inset>
        <van-cell title="我买到的" is-link to="/orders" />
        <van-cell title="我卖出的" is-link to="/orders?role=seller" />
      </van-cell-group>

      <div class="submit">
        <van-button block round @click="logout">退出登录</van-button>
      </div>
    </template>

    <template v-else>
      <van-empty description="登录后即可发布闲置、下单购买" />
      <div class="submit">
        <van-button type="primary" block round to="/login">去登录</van-button>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const store = useUserStore()
const router = useRouter()
const defaultAvatar = 'https://picsum.photos/seed/avatar/120/120'

const maskedPhone = computed(() => {
  const p = store.user?.phone
  return p ? `${p.slice(0, 3)}****${p.slice(7)}` : ''
})

onMounted(() => {
  if (store.isLogin && !store.user) {
    store.loadMe().catch(() => store.logout())
  }
})

function logout() {
  store.logout().finally(() => router.replace('/'))
}
</script>

<style scoped>
.user {
  display: flex;
  align-items: center;
  gap: 12px;
}

.nick {
  font-size: 16px;
  font-weight: 500;
}

.phone {
  font-size: 13px;
  color: #969799;
  margin-top: 4px;
}

.submit {
  padding: 24px 16px;
}
</style>
