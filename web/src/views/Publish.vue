<template>
  <div class="page">
    <van-nav-bar title="发布闲置家具" />

    <van-cell-group inset title="照片">
      <van-field
        v-model="form.imagesText"
        type="textarea"
        rows="2"
        autosize
        label="图片链接"
        placeholder="每行一个图片地址，第一张为主图"
      />
      <div class="hint">接入对象存储后改为上传组件（PRD 5.3 字段 1）</div>
    </van-cell-group>

    <van-cell-group inset title="基本信息">
      <van-field
        v-model="categoryName"
        readonly
        is-link
        label="品类"
        placeholder="请选择"
        @click="showPicker = true"
      />
      <van-field v-model="form.title" label="标题" maxlength="30" show-word-limit placeholder="品牌 + 品类 + 特征，5-30 字" />
      <van-field label="成色">
        <template #input>
          <van-radio-group v-model="form.itemCondition" direction="horizontal">
            <van-radio v-for="c in conditions" :key="c.value" :name="c.value">{{ c.text }}</van-radio>
          </van-radio-group>
        </template>
      </van-field>
      <van-field v-model="form.price" type="number" label="价格" placeholder="元" />
    </van-cell-group>

    <van-cell-group inset title="补充信息（选填，可提升成交）">
      <van-field v-model="form.lengthCm" type="number" label="长(cm)" />
      <van-field v-model="form.widthCm" type="number" label="宽(cm)" />
      <van-field v-model="form.heightCm" type="number" label="高(cm)" />
      <van-field v-model="form.material" label="材质" placeholder="实木 / 布艺 / 板式…" />
      <van-field v-model="form.style" label="风格" placeholder="北欧 / 日式…" />
      <van-field v-model="form.brand" label="品牌" />
      <van-field v-model="form.description" type="textarea" rows="3" autosize label="描述" maxlength="500" show-word-limit />
    </van-cell-group>

    <van-cell-group inset title="交易方式">
      <van-field label="方式">
        <template #input>
          <van-radio-group v-model="form.tradeType" direction="horizontal">
            <van-radio :name="1">仅自提</van-radio>
            <van-radio :name="2">仅配送</van-radio>
            <van-radio :name="3">都可以</van-radio>
          </van-radio-group>
        </template>
      </van-field>
      <van-field v-model="form.districtCode" label="所在区域" placeholder="如 110105" />
      <van-field v-model="form.address" label="详细地址" placeholder="下单后对买家可见" />
    </van-cell-group>

    <div class="submit">
      <van-button type="primary" block round :loading="submitting" @click="submit">提交审核</van-button>
    </div>

    <van-popup v-model:show="showPicker" position="bottom">
      <van-picker
        :columns="categoryColumns"
        @confirm="onCategoryConfirm"
        @cancel="showPicker = false"
      />
    </van-popup>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { getCategories, publishItem } from '@/api/item'
import type { CategoryNode } from '@/api/types'

const router = useRouter()
const showPicker = ref(false)
const submitting = ref(false)
const categoryName = ref('沙发座椅 / 三人沙发')

const conditions = [
  { text: '全新', value: 5 },
  { text: '几乎全新', value: 4 },
  { text: '轻微痕迹', value: 3 },
  { text: '明显痕迹', value: 2 },
  { text: '需维修', value: 1 }
]

const form = reactive({
  imagesText: 'https://picsum.photos/seed/mine1/600/600',
  categoryId: 11,
  title: '',
  itemCondition: 3,
  price: '',
  lengthCm: '',
  widthCm: '',
  heightCm: '',
  material: '',
  style: '',
  brand: '',
  description: '',
  tradeType: 1,
  districtCode: '110105',
  address: ''
})

const categoryColumns = ref<{ text: string; value: number }[]>([])

onMounted(async () => {
  const tree = await getCategories()
  categoryColumns.value = tree.flatMap((top: CategoryNode) =>
    top.children.map((sub) => ({ text: `${top.name} / ${sub.name}`, value: sub.id }))
  )
})

function onCategoryConfirm({ selectedOptions }: { selectedOptions: { text: string; value: number }[] }) {
  const picked = selectedOptions[0]
  categoryName.value = picked.text
  form.categoryId = picked.value
  showPicker.value = false
}

async function submit() {
  const images = form.imagesText
    .split('\n')
    .map((s) => s.trim())
    .filter(Boolean)

  if (images.length === 0) {
    showToast('至少上传一张图片')
    return
  }
  if (form.title.length < 5) {
    showToast('标题至少 5 个字')
    return
  }
  if (!form.price || Number(form.price) < 1) {
    showToast('请填写有效价格')
    return
  }

  submitting.value = true
  try {
    const payload: Record<string, unknown> = {
      images,
      categoryId: form.categoryId,
      title: form.title,
      itemCondition: form.itemCondition,
      price: Number(form.price),
      tradeType: form.tradeType,
      districtCode: form.districtCode,
      description: form.description || null,
      address: form.address || null
    }
    for (const key of ['lengthCm', 'widthCm', 'heightCm'] as const) {
      if (form[key]) payload[key] = Number(form[key])
    }
    for (const key of ['material', 'style', 'brand'] as const) {
      if (form[key]) payload[key] = form[key]
    }

    const id = await publishItem(payload)
    showToast('发布成功')
    router.replace(`/item/${id}`)
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.hint {
  font-size: 12px;
  color: #969799;
  padding: 8px 16px;
}

.submit {
  padding: 16px;
}
</style>
