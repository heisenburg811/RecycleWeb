<template>
  <div class="login">
    <div class="brand">
      <h1>旧物新生</h1>
      <p>让每一件家具再用十年</p>
    </div>

    <van-tabs v-model:active="tab">
      <van-tab title="登录">
        <van-cell-group inset>
          <van-field v-model="form.phone" label="手机号" placeholder="11 位手机号" maxlength="11" type="tel" />
          <van-field v-model="form.password" type="password" label="密码" placeholder="至少 6 位" />
        </van-cell-group>
      </van-tab>

      <van-tab title="注册">
        <van-cell-group inset>
          <van-field v-model="form.phone" label="手机号" placeholder="11 位手机号" maxlength="11" type="tel" />
          <van-field v-model="form.password" type="password" label="密码" placeholder="6-32 位" />
          <van-field v-model="form.nickname" label="昵称" placeholder="选填" maxlength="20" />
        </van-cell-group>
      </van-tab>
    </van-tabs>

    <div class="submit">
      <van-button type="primary" block round :loading="loading" @click="submit">
        {{ tab === 0 ? '登录' : '注册并登录' }}
      </van-button>
    </div>

    <div class="demo">演示账号：13800000001　密码：123456</div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const store = useUserStore()

const tab = ref(0)
const loading = ref(false)
const form = reactive({ phone: '', password: '', nickname: '' })

async function submit() {
  if (!/^1[3-9]\d{9}$/.test(form.phone)) {
    showToast('手机号格式不正确')
    return
  }
  if (form.password.length < 6) {
    showToast('密码至少 6 位')
    return
  }

  loading.value = true
  try {
    if (tab.value === 0) {
      await store.login(form.phone, form.password)
    } else {
      await store.register(form.phone, form.password, form.nickname || undefined)
    }
    showToast(tab.value === 0 ? '登录成功' : '注册成功')
    const redirect = route.query.redirect
    router.replace(typeof redirect === 'string' ? decodeURIComponent(redirect) : '/')
  } catch {
    // 错误提示已由请求层统一处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login {
  padding-top: 40px;
}

.brand {
  text-align: center;
  margin-bottom: 20px;
}

.brand h1 {
  margin: 0;
  font-size: 24px;
  letter-spacing: 2px;
}

.brand p {
  margin: 8px 0 0;
  font-size: 13px;
  color: #969799;
}

.submit {
  padding: 24px 16px 8px;
}

.demo {
  text-align: center;
  font-size: 12px;
  color: #969799;
}
</style>
