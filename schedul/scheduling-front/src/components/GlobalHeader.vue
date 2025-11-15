<template>
  <div id="globalHeader">
    <div class="header-container">
      <div class="header-left">
        <RouterLink to="/home">
          <div class="title-bar">
            <img class="logo" src="../assets/logo.png" alt="logo" />
            <div class="title">锦途集团</div>
          </div>
        </RouterLink>
      </div>
      <div class="header-center">
        <div class="nav-menu">
          <router-link to="/home" class="nav-item" :class="{ active: currentPath === '/home' }">
            主页
          </router-link>
          <router-link
            v-if="isAdmin"
            to="/admin/userManage"
            class="nav-item"
            :class="{ active: currentPath === '/admin/userManage' }"
          >
            用户管理
          </router-link>
        </div>
      </div>
      <div class="header-right">
        <div class="user-login-status">
          <div v-if="loginUserStore.loginUser.id">
            <el-dropdown @command="handleCommand">
              <span class="user-dropdown-trigger">
                <el-icon style="margin-right: 4px;"><User /></el-icon>
                {{ loginUserStore.loginUser.userName || '无名' }}
                <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="info">
                    <el-icon><User /></el-icon>
                    <span style="margin-left: 8px;">我的信息</span>
                  </el-dropdown-item>
                  <el-dropdown-item command="logout" divided>
                    <el-icon><SwitchButton /></el-icon>
                    <span style="margin-left: 8px;">退出登录</span>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
          <div v-else>
            <el-button type="primary" @click="goToLogin">登录</el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { RouterLink } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, ArrowDown, SwitchButton } from '@element-plus/icons-vue'
import { useLoginUserStore } from '@/stores/useLoginUserStore'
import { userLoginOutUsingPost } from '@/api/index'

// 初始化 Pinia Store
const loginUserStore = useLoginUserStore()
// 初始化路由
const router = useRouter()
const route = useRoute()

// 当前路径
const currentPath = computed(() => route.path)

// 判断是否为管理员
const isAdmin = computed(() => {
  // 优先从 store 获取
  if (loginUserStore.loginUser && loginUserStore.loginUser.userRole === 'admin') {
    return true
  }
  // 如果 store 中没有，从 localStorage 获取
  try {
    const userInfo = localStorage.getItem('userInfo')
    if (userInfo) {
      const user = JSON.parse(userInfo)
      return user.userRole === 'admin'
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
  return false
})

// 下拉菜单命令处理
const handleCommand = (command) => {
  if (command === 'info') {
    router.push('/my_information')
  } else if (command === 'logout') {
    doLogout()
  }
}

// 跳转到登录页
const goToLogin = () => {
  router.push('/user/login')
}

// 退出登录
const doLogout = async () => {
  // 先清除本地数据（无论后端请求是否成功）
  const clearLocalData = () => {
    // 重置 Pinia 中的用户信息
    loginUserStore.setLoginUser({
      userName: '',
      id: '', // 清空 id，让登录状态判断失效
      userRole: ''
    })
    // 清除本地存储的用户信息
    localStorage.removeItem('userInfo')
    // 清除 token
    localStorage.removeItem('vue_login_token')
  }

  try {
    const res = await userLoginOutUsingPost()
    console.log(res)
    
    // 无论后端返回什么，都清除本地数据
    clearLocalData()
    
    // 适配后端返回格式：code=0 为成功
    if (res.code === 0 || res.data?.code === 0) {
      ElMessage.success('退出登录成功')
    } else {
      // 即使后端返回错误（如"未登录"），也允许退出
      // 可能是 session 已过期，但用户点击了退出，应该允许退出
      const errorMsg = res.data?.message || res.message || ''
      if (errorMsg.includes('未登录') || errorMsg.includes('登录')) {
        ElMessage.success('已退出登录')
      } else {
        ElMessage.warning('退出登录：' + errorMsg)
      }
    }
    
    // 跳转到登录页
    router.push({ path: '/user/login' })
  } catch (err) {
    console.error('退出登录请求失败：', err)
    
    // 即使请求失败（网络错误、后端错误等），也清除本地数据并退出
    clearLocalData()
    
    // 检查是否是"未登录"相关的错误
    const errorMsg = err.response?.data?.message || err.message || ''
    if (errorMsg.includes('未登录') || errorMsg.includes('登录')) {
      ElMessage.success('已退出登录')
    } else {
      ElMessage.warning('退出登录：' + (errorMsg || '网络异常，已清除本地登录信息'))
    }
    
    router.push({ path: '/user/login' })
  }
}
</script>

<style scoped>
#globalHeader {
  background-color: #ffffff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  padding: 0 24px;
  height: 64px;
  box-sizing: border-box;
}

.header-container {
  display: flex;
  align-items: center;
  height: 100%;
  max-width: 100%;
}

.header-left {
  flex: 0 0 200px;
  display: flex;
  align-items: center;
}

.title-bar {
  display: flex;
  align-items: center;
  text-decoration: none;
  color: inherit;
}

.title {
  color: black;
  font-size: 18px;
  margin-left: 16px;
  font-weight: 600;
}

.logo {
  height: 24px;
}

.header-center {
  flex: 1;
  display: flex;
  justify-content: center;
}

.nav-menu {
  display: flex;
  align-items: center;
  gap: 24px;
}

.nav-item {
  padding: 8px 16px;
  text-decoration: none;
  color: #606266;
  font-size: 14px;
  border-radius: 4px;
  transition: all 0.3s;
}

.nav-item:hover {
  color: #409eff;
  background-color: #ecf5ff;
}

.nav-item.active {
  color: #409eff;
  font-weight: 600;
}

.header-right {
  flex: 0 0 200px;
  display: flex;
  justify-content: flex-end;
  align-items: center;
}

.user-login-status {
  display: flex;
  align-items: center;
}

.user-dropdown-trigger {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 4px;
  transition: background-color 0.3s;
  color: #606266;
  font-size: 14px;
}

.user-dropdown-trigger:hover {
  background-color: #f5f7fa;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header-left {
    flex: 0 0 150px;
  }
  
  .header-center {
    display: none;
  }
  
  .header-right {
    flex: 1;
  }
  
  .title {
    font-size: 16px;
    margin-left: 8px;
  }
}
</style>
