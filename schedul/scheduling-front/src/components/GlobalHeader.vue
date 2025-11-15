<template>
  <div id="globalHeader">
    <a-row :wrap="false">
      <a-col flex="200px">
        <RouterLink to="/">
          <div class="title-bar">
            <img class="logo" src="../assets/logo.png" alt="logo" />
            <div class="title">锦途集团</div>
          </div>
        </RouterLink>
      </a-col>
      <a-col flex="auto">
        <a-menu
          v-model:selectedKeys="current"
          mode="horizontal"
          :items="items"
          @click="doMenuClick"
        />
      </a-col>
      <a-col flex="120px">
        <div class="user-login-status">
          <div v-if="loginUserStore.loginUser.id">
            <a-dropdown>
              <a-space>
                <!-- <a-avatar :src="loginUserStore.loginUser.userAvatar"></a-avatar> -->
                {{ loginUserStore.loginUser.userName ?? '无名' }}
              </a-space>
              <template #overlay>
                <a-menu-item>
                  <router-link to="/my_information">
                    <UserOutlined />
                    我的信息
                  </router-link>
                </a-menu-item>
                <a-menu-item @click="doLogout">
                  <LogoutOutlined />
                  退出登录
                </a-menu-item>
              </template>
            </a-dropdown>
          </div>
          <div v-else>
            <a-button type="primary" href="/user/login">登录</a-button>
          </div>
        </div>
      </a-col>
    </a-row>
  </div>
</template>

<script setup>

import { useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/useLoginUserStore' // 注意：JS 版 Store 路径可能需去掉 .ts 后缀
// 引入退出登录接口
import { userLoginOutUsingPost } from '@/api/index'
import { ref, computed } from 'vue'
import { RouterLink } from 'vue-router'

// 初始化 Pinia Store
const loginUserStore = useLoginUserStore()
// 初始化路由
const router = useRouter()

// 原始菜单数据（移除 TS 类型注解）
const originItems = [
  {
    key: '/',
    icon: () => h(HomeOutlined),
    label: '主页',
    title: '主页',
  },
  {
    key: '/admin/userManage',
    label: '用户管理',
    title: '用户管理',
  },
  {
    key: '/admin/pictureManage',
    label: '图片管理',
    title: '图片管理',
  },
  {
    key: '/admin/spaceManage',
    label: '空间管理',
    title: '空间管理',
  },
  {
    key: '/add_picture',
    label: '创建图片',
    title: '创建图片',
  },

]

// 过滤菜单（管理员才显示 /admin 开头的菜单）
const filterMenus = (menus = []) => {
  if (!Array.isArray(menus)) {
    return []
  }
  return menus.filter((menu) => {
    // 管理员菜单过滤逻辑
    if (menu.key.startsWith('/admin')) {
      const loginUser = loginUserStore.loginUser
      // 非登录状态或非管理员角色，隐藏该菜单
      if (!loginUser || loginUser.userRole !== 'admin') {
        return false
      }
    }
    return true
  })
}

// 计算属性：动态生成过滤后的菜单
const items = computed(() => filterMenus(originItems))

// 当前选中菜单（默认空数组）
const current = ref([])

// 路由跳转事件
const doMenuClick = ({ key }) => {
  // 仅处理路由路径类型的菜单（排除外部链接）
  if (key.startsWith('/')) {
    router.push({
      path: key,
    })
  }
}

// 退出登录
const doLogout = async () => {
  try {
    const res = await userLoginOutUsingPost()
    console.log(res)
    // 适配后端返回格式：code=0 为成功
    if (res.data.code === 0) {
      // 重置 Pinia 中的用户信息
      loginUserStore.setLoginUser({
        userName: '',
        id: '', // 清空 id，让登录状态判断失效
        userRole: ''
      })
      // 清除本地存储的用户信息（如果之前存储过）
      localStorage.removeItem('userInfo')
      message.success('退出登录成功')
      // 跳转到登录页
      router.push({ path: '/user/login' })
    } else {
      message.error('退出登录失败，' + (res.data.message || '请重试'))
    }
  } catch (err) {
    console.error('退出登录请求失败：', err)
    message.error('退出登录失败，网络异常')
  }
}

// 监听路由变化，更新当前选中菜单
router.afterEach((to) => {
  current.value = [to.path]
})
</script>

<style scoped>
.title-bar {
  display: flex;
  align-items: center;
}

.title {
  color: black;
  font-size: 18px;
  margin-left: 16px;
}

.logo {
  height: 24px;
}

/* 调整用户区域样式，避免溢出 */
.user-login-status {
  display: flex;
  align-items: center;
  justify-content: flex-end;
}

/* 适配下拉菜单样式 */
.ant-dropdown-menu-item {
  cursor: pointer;
}
</style>