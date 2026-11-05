<template>
  <div class="page">
    <van-nav-bar :title="editId ? '编辑商品' : '发布闲置家具'" left-arrow @click-left="router.back()" />

    <van-cell-group inset title="照片">
      <div class="uploader">
        <van-uploader
          v-model="fileList"
          :max-count="9"
          :after-read="afterRead"
          accept="image/*"
          multiple
        />
      </div>
      <div class="hint">第一张为主图，最多 9 张；单张不超过 5MB</div>
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
      <van-button type="primary" block round :loading="submitting" @click="submit">
        {{ editId ? '保存修改' : '提交审核' }}
      </van-button>
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
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import type { UploaderFileListItem } from 'vant'
import { getCategories, getItem, publishItem, updateItem, uploadImage } from '@/api/item'
import type { CategoryNode } from '@/api/types'

const router = useRouter()
const route = useRoute()

const editId = computed(() => (route.query.id ? Number(route.query.id) : null))
const showPicker = ref(false)
const submitting = ref(false)
const categoryName = ref('沙发座椅 / 三人沙发')
const fileList = ref<UploaderFileListItem[]>([])

const conditions = [
  { text: '全新', value: 5 },
  { text: '几乎全新', value: 4 },
  { text: '轻微痕迹', value: 3 },
  { text: '明显痕迹', value: 2 },
  { text: '需维修', value: 1 }
]

const form = reactive({
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

  if (editId.value) {
    const item = await getItem(editId.value)
    form.categoryId = item.categoryId
    form.title = item.title
    form.itemCondition = item.itemCondition
    form.price = String(item.price)
    form.lengthCm = item.lengthCm ? String(item.lengthCm) : ''
    form.widthCm = item.widthCm ? String(item.widthCm) : ''
    form.heightCm = item.heightCm ? String(item.heightCm) : ''
    form.material = item.material ?? ''
    form.style = item.style ?? ''
    form.brand = item.brand ?? ''
    form.description = item.description ?? ''
    form.tradeType = item.tradeType
    form.districtCode = item.districtCode
    fileList.value = item.images.map((url) => ({ url }))
    const matched = categoryColumns.value.find((c) => c.value === item.categoryId)
    if (matched) categoryName.value = matched.text
  }
})

/** 选完图立刻上传，成功就把本地预览换成服务器地址 */
async function afterRead(items: UploaderFileListItem | UploaderFileListItem[]) {
  const list = Array.isArray(items) ? items : [items]
  for (const item of list) {
    if (!item.file) continue
    item.status = 'uploading'
    item.message = '上传中'
    try {
      const url = await uploadImage(item.file)
      item.url = url
      item.status = 'done'
      item.message = ''
    } catch {
      item.status = 'failed'
      item.message = '上传失败'
    }
  }
}

function onCategoryConfirm({ selectedOptions }: { selectedOptions: { text: string; value: number }[] }) {
  const picked = selectedOptions[0]
  categoryName.value = picked.text
  form.categoryId = picked.value
  showPicker.value = false
}

async function submit() {
  const images = fileList.value
    .filter((f) => f.status === 'done' && f.url && f.url.startsWith('/uploads'))
    .map((f) => f.url as string)

  if (images.length === 0) {
    showToast('请至少上传一张图片')
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

    if (editId.value) {
      await updateItem(editId.value, payload)
      showToast('已保存')
      router.replace(`/item/${editId.value}`)
    } else {
      const id = await publishItem(payload)
      showToast('发布成功')
      router.replace(`/item/${id}`)
    }
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.uploader {
  padding: 12px 16px;
}

.hint {
  font-size: 12px;
  color: #969799;
  padding: 0 16px 12px;
}

.submit {
  padding: 16px;
}
</style>
