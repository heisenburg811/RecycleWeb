<template>
  <div class="page">
    <van-nav-bar :title="isReschedule ? '改约配送' : '预约配送'" left-arrow @click-left="router.back()" />

    <van-cell-group inset title="选择日期">
      <div class="dates">
        <div
          v-for="d in dates"
          :key="d.value"
          class="date"
          :class="{ active: d.value === selectedDate }"
          @click="pickDate(d.value)"
        >
          <div class="week">{{ d.week }}</div>
          <div class="day">{{ d.day }}</div>
        </div>
      </div>
    </van-cell-group>

    <van-cell-group inset title="选择时段">
      <van-loading v-if="loading" class="loading" />
      <div v-else class="slots">
        <div
          v-for="s in slots"
          :key="s.code"
          class="slot"
          :class="{ active: s.code === selectedSlot, disabled: !s.available }"
          @click="pickSlot(s)"
        >
          <div class="slot-time">{{ s.code }}</div>
          <div class="slot-left">{{ s.available ? `剩 ${s.remaining} 单` : '不可约' }}</div>
        </div>
      </div>
    </van-cell-group>

    <div class="tip">
      配送当日不可改约；每单可免费改约 1 次。当前时段若已开始，预约后会尽快安排出发。
    </div>

    <div class="submit">
      <van-button type="primary" block round :loading="submitting" @click="submit">
        {{ isReschedule ? '确认改约' : '确认预约' }}
      </van-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import { bookLogistics, getSlots, rescheduleLogistics } from '@/api/logistics'
import type { SlotVO } from '@/api/logistics-types'

const route = useRoute()
const router = useRouter()

const orderNo = computed(() => String(route.params.orderNo))
const isReschedule = computed(() => route.query.mode === 'reschedule')

const slots = ref<SlotVO[]>([])
const loading = ref(false)
const submitting = ref(false)
const selectedDate = ref('')
const selectedSlot = ref('')

const WEEK = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']

const dates = Array.from({ length: 8 }, (_, i) => {
  const d = new Date()
  d.setDate(d.getDate() + i)
  const iso = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
  return {
    value: iso,
    week: i === 0 ? '今天' : i === 1 ? '明天' : WEEK[d.getDay()],
    day: `${d.getMonth() + 1}/${d.getDate()}`
  }
})

async function loadSlots() {
  loading.value = true
  selectedSlot.value = ''
  try {
    slots.value = await getSlots(selectedDate.value)
  } catch {
    slots.value = []
  } finally {
    loading.value = false
  }
}

function pickDate(value: string) {
  if (value === selectedDate.value) return
  selectedDate.value = value
  loadSlots()
}

function pickSlot(slot: SlotVO) {
  if (!slot.available) {
    showToast('该时段不可预约')
    return
  }
  selectedSlot.value = slot.code
}

async function submit() {
  if (!selectedSlot.value) {
    showToast('请选择配送时段')
    return
  }
  submitting.value = true
  try {
    if (isReschedule.value) {
      await rescheduleLogistics(orderNo.value, selectedDate.value, selectedSlot.value)
      showToast('改约成功')
    } else {
      await bookLogistics(orderNo.value, selectedDate.value, selectedSlot.value)
      showToast('预约成功')
    }
    router.replace(`/order/${orderNo.value}`)
  } catch {
    // 请求层已提示
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  selectedDate.value = dates[0].value
  loadSlots()
})
</script>

<style scoped>
.dates {
  display: flex;
  gap: 8px;
  padding: 12px 16px;
  overflow-x: auto;
}

.date {
  flex: 0 0 auto;
  width: 56px;
  padding: 8px 0;
  text-align: center;
  border-radius: 8px;
  background: #f7f8fa;
  border: 1px solid transparent;
}

.date.active {
  background: #e8f3ff;
  border-color: #1989fa;
  color: #1989fa;
}

.week {
  font-size: 12px;
}

.day {
  font-size: 14px;
  font-weight: 600;
  margin-top: 2px;
}

.slots {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
  padding: 12px 16px;
}

.slot {
  padding: 10px 12px;
  border-radius: 8px;
  background: #f7f8fa;
  border: 1px solid transparent;
  text-align: center;
}

.slot.active {
  background: #e8f3ff;
  border-color: #1989fa;
  color: #1989fa;
}

.slot.disabled {
  opacity: 0.45;
}

.slot-time {
  font-size: 14px;
  font-weight: 600;
}

.slot-left {
  font-size: 12px;
  color: #969799;
  margin-top: 4px;
}

.loading {
  padding: 20px;
  text-align: center;
  width: 100%;
}

.tip {
  font-size: 12px;
  color: #969799;
  padding: 4px 20px 0;
  line-height: 1.6;
}

.submit {
  padding: 20px 16px;
}
</style>
