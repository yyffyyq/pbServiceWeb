<template>
  <div class="register-page">
    <div class="register-box">
      <h1 class="register-title">用户注册</h1>
      
      <!-- 注册表单 -->
      <form @submit.prevent="handleRegister" class="register-form">
        <!-- 账号输入 -->
        <div class="form-group">
          <label for="userAccount" class="form-label">用户账号</label>
          <input
            id="userAccount"
            v-model.trim="form.userAccount"
            type="text"
            class="form-input"
            placeholder="请输入4-20位账号（仅支持字母/数字）"
            @blur="validateAccount"
            @input="clearError('userAccount')"
          />
          <span v-if="formErrors.userAccount" class="error-tip">{{ formErrors.userAccount }}</span>
        </div>

        <!-- 新增：用户名输入 -->
        <div class="form-group">
          <label for="userName" class="form-label">用户名</label>
          <input
            id="userName"
            v-model.trim="form.userName"
            type="text"
            class="form-input"
            placeholder="请输入2-10位用户名（支持中文/字母/数字）"
            @blur="validateUserName"
            @input="clearError('userName')"
          />
          <span v-if="formErrors.userName" class="error-tip">{{ formErrors.userName }}</span>
        </div>

        <!-- 新增：手机号输入 -->
        <div class="form-group">
          <label for="phone" class="form-label">手机号</label>
          <input
            id="phone"
            v-model.trim="form.phone"
            type="tel"
            class="form-input"
            placeholder="请输入11位手机号"
            @blur="validatePhone"
            @input="clearError('phone')"
          />
          <span v-if="formErrors.phone" class="error-tip">{{ formErrors.phone }}</span>
        </div>

        <!-- 新增：部门选择 -->
        <div class="form-group">
          <label for="dept" class="form-label">所属部门</label>
          <select
            id="dept"
            v-model="form.dept"
            class="form-input"
            @change="clearError('dept')"
            @blur="validateDept"
          >
            <option value="">请选择部门</option>
            <option value="技术部">技术部</option>
            <option value="产品部">产品部</option>
            <option value="运营部">运营部</option>
            <option value="市场部">市场部</option>
            <option value="行政部">行政部</option>
            <option value="财务部">财务部</option>
          </select>
          <span v-if="formErrors.dept" class="error-tip">{{ formErrors.dept }}</span>
        </div>

        <!-- 密码输入 -->
        <div class="form-group">
          <label for="userPassword" class="form-label">登录密码</label>
          <input
            id="userPassword"
            v-model.trim="form.userPassword"
            type="password"
            class="form-input"
            placeholder="请输入8-20位密码（需包含字母+数字）"
            @blur="validatePassword"
            @input="clearError('userPassword')"
          />
          <span v-if="formErrors.userPassword" class="error-tip">{{ formErrors.userPassword }}</span>
        </div>

        <!-- 确认密码输入 -->
        <div class="form-group">
          <label for="checkPassword" class="form-label">确认密码</label>
          <input
            id="checkPassword"
            v-model.trim="form.checkPassword"
            type="password"
            class="form-input"
            placeholder="请再次输入密码"
            @blur="validateCheckPassword"
            @input="clearError('checkPassword')"
          />
          <span v-if="formErrors.checkPassword" class="error-tip">{{ formErrors.checkPassword }}</span>
        </div>

        <!-- 操作区域 -->
        <div class="form-actions">
          <button
            type="submit"
            class="register-btn"
            :disabled="isSubmitting"
          >
            <span v-if="!isSubmitting">立即注册</span>
            <span v-if="isSubmitting">注册中...</span>
          </button>
          <a href="/login" class="login-link">已有账号？前往登录</a>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import request from '@/utils/request'; // 引入你配置的axios实例
import { register } from '@/api'; // 引入注册API函数
import { ElMessage } from 'element-plus';

// 表单数据（新增userName、phone、dept字段）
const form = ref({
  userAccount: '',
  userName: '', // 新增：用户名
  phone: '',    // 新增：手机号
  dept: '',     // 新增：部门
  userPassword: '',
  checkPassword: ''
});

// 表单错误提示（新增对应字段的错误提示）
const formErrors = ref({
  userAccount: '',
  userName: '',
  phone: '',
  dept: '',
  userPassword: '',
  checkPassword: ''
});

// 提交状态（防止重复提交）
const isSubmitting = ref(false);

// 清除单个字段错误
const clearError = (field) => {
  formErrors.value[field] = '';
};

// 账号校验（原有逻辑不变）
const validateAccount = () => {
  const account = form.value.userAccount.trim();
  if (!account) {
    formErrors.value.userAccount = '账号不能为空';
  }  else {
    formErrors.value.userAccount = '';
  }
};

// 新增：用户名校验
const validateUserName = () => {
  const userName = form.value.userName.trim();
  if (!userName) {
    formErrors.value.userName = '用户名不能为空';
  } else if (userName.length < 2 || userName.length > 10) {
    formErrors.value.userName = '用户名长度需在2-10位之间';
  } else if (!/^[a-zA-Z0-9\u4e00-\u9fa5]+$/.test(userName)) {
    formErrors.value.userName = '用户名仅支持中文、字母和数字组合';
  } else {
    formErrors.value.userName = '';
  }
};

// 新增：手机号校验
const validatePhone = () => {
  const phone = form.value.phone.trim();
  if (!phone) {
    formErrors.value.phone = '手机号不能为空';
  } else if (!/^1[3-9]\d{9}$/.test(phone)) {
    formErrors.value.phone = '请输入有效的11位手机号';
  } else {
    formErrors.value.phone = '';
  }
};

// 新增：部门校验
const validateDept = () => {
  const dept = form.value.dept;
  if (!dept) {
    formErrors.value.dept = '请选择所属部门';
  } else {
    formErrors.value.dept = '';
  }
};

// 密码校验（原有逻辑不变）
const validatePassword = () => {
  const password = form.value.userPassword.trim();
  if (!password) {
    formErrors.value.userPassword = '密码不能为空';
  } else if (password.length < 8 || password.length > 20) {
    formErrors.value.userPassword = '密码长度需在8-20位之间';
  }else {
    formErrors.value.userPassword = '';
  }
};

// 确认密码校验（原有逻辑不变）
const validateCheckPassword = () => {
  const password = form.value.userPassword.trim();
  const checkPwd = form.value.checkPassword.trim();
  if (!checkPwd) {
    formErrors.value.checkPassword = '请再次输入密码';
  } else if (checkPwd !== password) {
    formErrors.value.checkPassword = '两次输入的密码不一致';
  } else {
    formErrors.value.checkPassword = '';
  }
};

// 整体表单校验（提交前，新增对三个字段的校验）
const validateForm = () => {
  validateAccount();
  validateUserName(); // 新增
  validatePhone();    // 新增
  validateDept();     // 新增
  validatePassword();
  validateCheckPassword();
  // 检查是否有错误
  return Object.values(formErrors.value).every(error => !error);
};

// 注册提交逻辑（表单数据会自动包含新增字段，无需额外修改）
const handleRegister = async () => {
  // 表单校验
  if (!validateForm()) return;

  // 防止重复提交
  isSubmitting.value = true;

  try {
    // 发送注册请求（form.value 已包含所有字段：userAccount、userName、phone、dept、userPassword、checkPassword）
    const res = await register(form.value);

    // 适配后端返回格式（code为0或200视为成功，根据实际调整）
    if (res.code === 0 || res.code === 200) {
      ElMessage.success('注册成功！即将跳转登录页');
      // 跳转登录页（根据你的路由配置修改路径）
      setTimeout(() => {
        window.location.href = '/login';
      }, 1500);
    } else {
      ElMessage.error(`注册失败：${res.msg || '请检查输入信息'}`);
    }
  } catch (error) {
    console.error('注册失败', error);
    ElMessage.error('注册失败！网络错误或服务器异常');
  } finally {
    isSubmitting.value = false;
  }
};
</script>

<style scoped>
/* 原有样式不变，新增字段会自动适配样式 */
.register-page {
  width: 100%;
  height: 100vh;
  background-color: #f8f9fa;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
  box-sizing: border-box;
}

.register-box {
  width: 100%;
  max-width: 500px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  padding: 40px;
  box-sizing: border-box;
}

.register-title {
  font-size: 28px;
  font-weight: 600;
  color: #333;
  margin: 0 0 30px 0;
  text-align: center;
}

.register-form {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  font-size: 14px;
  color: #666;
  font-weight: 500;
}

/* 适配下拉选择框样式 */
.form-input {
  height: 48px;
  padding: 0 16px;
  border: 1px solid #e5e6eb;
  border-radius: 6px;
  font-size: 16px;
  color: #333;
  transition: all 0.3s ease;
  box-sizing: border-box;
  appearance: none; /* 清除默认下拉箭头 */
  background: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='16' height='16' viewBox='0 0 24 24' fill='none' stroke='%23666' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpolyline points='6 9 12 15 18 9'%3E%3C/polyline%3E%3C/svg%3E") no-repeat right 16px center;
  background-size: 16px;
}

.form-input:focus {
  outline: none;
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.1);
}

.form-input::placeholder {
  color: #c9c9c9;
}

.error-tip {
  font-size: 12px;
  color: #f5222d;
  margin-top: 4px;
}

.form-actions {
  margin-top: 12px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.register-btn {
  height: 50px;
  background-color: #1890ff;
  color: #fff;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.register-btn:hover:not(:disabled) {
  background-color: #096dd9;
}

.register-btn:disabled {
  background-color: #8cc5ff;
  cursor: not-allowed;
}

.login-link {
  text-align: center;
  font-size: 14px;
  color: #666;
  text-decoration: none;
}

.login-link:hover {
  color: #1890ff;
  text-decoration: underline;
}
</style>