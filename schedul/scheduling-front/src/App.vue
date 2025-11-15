<template>
  <!-- 顶部固定导航栏 -->
  <GlobalHeader class="global-header" />
  <!-- 页面内容区域（预留导航栏高度，避免遮挡） -->
  <div class="app-content">
    <RouterView />
  </div>
</template>

<script setup>
import { RouterView } from 'vue-router';
import GlobalHeader from '@/components/GlobalHeader.vue'; // 确保 GlobalHeader 组件路径正确

// 无需额外逻辑，仅保留组件引入和路由视图
</script>

<style scoped>
/* 顶部导航栏样式：固定定位 + 样式优化 */
.global-header {
  position: fixed; /* 固定在顶部 */
  top: 0;
  left: 0;
  right: 0;
  z-index: 999; /* 确保在页面内容上方 */
  background-color: #ffffff; /* 白色背景 */
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08); /* 轻微阴影，提升层次感 */
  padding: 0 24px; /* 左右内边距，避免内容贴边 */
  height: 64px; /* 固定导航栏高度，与内部元素对齐 */
  box-sizing: border-box; /* 确保 padding 不影响总高度 */
}

/* 页面内容区域：预留导航栏高度，避免被遮挡 */
.app-content {
  margin-top: 64px; /* 与导航栏高度一致 */
  min-height: calc(100vh - 64px); /* 内容区域最小高度 = 屏幕高度 - 导航栏高度 */
  box-sizing: border-box;
  padding: 20px; /* 内容内边距，提升阅读体验 */
  background-color: #f5f7fa; /* 浅灰色背景，区分导航栏和内容区 */
}
</style>