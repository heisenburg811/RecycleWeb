<template>
  <router-view />
  <van-tabbar route>
    <van-tabbar-item replace to="/" icon="home-o">首页</van-tabbar-item>
    <van-tabbar-item replace to="/publish" icon="add-o">发布</van-tabbar-item>
    <van-tabbar-item replace to="/orders" icon="orders-o">订单</van-tabbar-item>
    <van-tabbar-item replace to="/me" icon="user-o">我的</van-tabbar-item>
  </van-tabbar>
</template>

<script setup lang="ts"></script>
