import { defineStore } from 'pinia'
import { ref } from 'vue'
// 引入后端接口（确保路径与你的项目一致）
import { getLoginUserUsingGet } from '@/api/index' // 假设该接口已存在

// 定义登录用户的 Pinia Store（JS 版）
export const useLoginUserStore = defineStore('loginUser', () => {
  // 初始状态：默认未登录
  const loginUser = ref({
    userName: '未登录',
    // 可补充其他默认字段（与后端返回 data 结构对齐）
    id: '',
    userAccount: '',
    userRole: 'user',
    createTime: null,
    updateTime: null
  })

  /** 获取登录用户信息（调用后端接口同步用户数据） */
  async function fetchLoginUser() {
    const res = await getLoginUserUsingGet()
    // 适配后端返回格式：code=0 且有数据时更新状态
    if (res.data.code === 0 && res.data.data) {
      loginUser.value = res.data.data
    }
  }

  /** 手动设置登录用户信息（如登录成功后直接赋值） */
  function setLoginUser(newLoginUser) {
    loginUser.value = newLoginUser
  }

  return { loginUser, setLoginUser, fetchLoginUser }
})