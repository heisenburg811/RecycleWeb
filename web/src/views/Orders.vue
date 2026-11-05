<template>
  <div class="page">
    <van-nav-bar title="我的订单" />
    <van-empty description="下单 / 支付 / 物流预约在下一迭代接入" />
    <div class="tips">
      <p>已实现的接口：</p>
      <ul>
        <li>POST /api/v1/orders/quote —— 费用试算（运费 / 楼层 / 拆装 / 服务费）</li>
        <li>GET /api/v1/items —— 商品列表与搜索</li>
        <li>GET /api/v1/items/{id} —— 商品详情</li>
        <li>POST /api/v1/items —— 发布商品</li>
        <li>GET /api/v1/categories —— 类目树</li>
      </ul>
    </div>
  </div>
</template>

<script setup lang="ts"></script>

<style scoped>
.tips {
  padding: 0 24px;
  font-size: 13px;
  color: #969799;
  line-height: 1.8;
}
</style>
