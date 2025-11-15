<template>
  <div class="login-container">
    <div class="login-box">
      <h2 class="login-title">系统登录</h2>
      <el-form 
        ref="loginFormRef" 
        :model="loginForm" 
        :rules="loginRules" 
        label-position="top"
        class="login-form"
      >
        <el-form-item label="账号" prop="userAccount">
          <el-input 
            v-model="loginForm.userAccount" 
            placeholder="请输入账号" 
            prefix-icon="User"
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item label="密码" prop="userPassword">
          <el-input 
            v-model="loginForm.userPassword" 
            type="password" 
            placeholder="请输入密码" 
            prefix-icon="Lock"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item style="width: 100%">
          <el-button 
            type="primary" 
            :loading="loading" 
            style="width: 100%" 
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { ElMessage } from 'element-plus';
import { useRouter } from 'vue-router';
import { getLogin } from '@/api/index';

const router = useRouter();
const loginFormRef = ref(null); // 表单引用
const loading = ref(false); // 登录按钮加载状态

// 登录表单数据（与后端请求字段一致：userAccount、userPassword）
const loginForm = reactive({
  userAccount: '',
  userPassword: ''
});

// 表单校验规则
const loginRules = {
  userAccount: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 3, max: 20, message: '账号长度在 3 到 20 个字符之间', trigger: 'blur' }
  ],
  userPassword: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符之间', trigger: 'blur' }
  ]
};

// 登录处理逻辑
const handleLogin = () => {
  // 表单校验
  loginFormRef.value.validate((valid) => {
    if (!valid) return; // 校验失败直接返回

    loading.value = true; // 开启加载状态
    getLogin(loginForm)
      .then((res) => {
        // 后端返回 code: 0 表示登录成功（与后端格式对齐）
        if (res.code === 0) {
          const userInfo = res.data; // 后端返回的用户信息（id、userAccount、userRole 等）
          
          // 1. 存储用户信息到本地缓存（页面刷新不丢失）
          localStorage.setItem('userInfo', JSON.stringify(userInfo));
          
          // 2. 可选：如果后端后续接口需要 Token，可让后端在 data 中返回 token 字段，此处存储
          // localStorage.setItem('token', userInfo.token);

          ElMessage.success('登录成功！');
          // 跳转到首页（根据你的路由配置调整）
          router.push('/');
        } else {
          // 登录失败（如账号密码错误），显示后端返回的错误信息
          ElMessage.error(res.message || '登录失败，请重试');
        }
      })
      .catch((err) => {
        // 网络异常或接口报错
        ElMessage.error('网络异常，请检查后端服务是否正常');
        console.error('登录请求失败：', err);
      })
      .finally(() => {
        loading.value = false; // 关闭加载状态
      });
  });
};
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  margin: 0;
  background: linear-gradient(135deg, #e8f4f8 0%, #f0f8fb 100%);
}

.login-box {
  width: 100%;
  max-width: 420px;
  padding: 32px;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.login-title {
  text-align: center;
  margin-bottom: 28px;
  color: #2d3748;
  font-size: 20px;
  font-weight: 600;
}

.login-form {
  width: 100%;
}

/* 调整输入框和按钮样式，优化交互体验 */
.el-input {
  border-radius: 8px;
}

.el-button {
  border-radius: 8px;
  height: 44px;
  font-size: 16px;
}
</style>