<template>
  <div class="user-manage-page" v-if="pageReady">
    <div class="page-header">
      <h1 class="page-title">用户管理</h1>
      <el-button type="primary" :icon="Plus" @click="handleAdd"
        >添加用户</el-button
      >
    </div>

    <!-- 搜索表单 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="searchParams" inline>
        <el-form-item label="账号">
          <el-input
            v-model="searchParams.userAccount"
            placeholder="请输入账号"
            clearable
            style="width: 200px"
            @keyup.enter="doSearch"
          />
        </el-form-item>
        <el-form-item label="用户名">
          <el-input
            v-model="searchParams.userName"
            placeholder="请输入用户名"
            clearable
            style="width: 200px"
            @keyup.enter="doSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="doSearch"
            >搜索</el-button
          >
          <el-button :icon="Refresh" @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 用户表格 -->
    <el-card class="table-card" shadow="never">
      <el-table
        :data="dataList"
        v-loading="loading"
        border
        stripe
        style="width: 100%"
        :fit="true"
      >
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column
          prop="userAccount"
          label="账号"
          min-width="120"
          show-overflow-tooltip
        />
        <el-table-column
          prop="userName"
          label="用户名"
          min-width="100"
          show-overflow-tooltip
        />
        <el-table-column
          prop="phone"
          label="手机号"
          min-width="120"
          show-overflow-tooltip
        >
          <template #default="{ row }">
            {{ row.phone || "未设置" }}
          </template>
        </el-table-column>
        <el-table-column
          prop="dept"
          label="部门"
          min-width="120"
          show-overflow-tooltip
        >
          <template #default="{ row }">
            {{ row.dept || "未设置" }}
          </template>
        </el-table-column>
        <el-table-column
          prop="userRole"
          label="用户角色"
          width="120"
          align="center"
        >
          <template #default="{ row }">
            <el-tag v-if="row.userRole === 'admin'" type="success"
              >管理员</el-tag
            >
            <el-tag v-else type="info">普通用户</el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="createTime"
          label="创建时间"
          width="180"
          align="center"
        >
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="本月值班" width="110" align="center">
          <template #default="{ row }">
            <el-tag type="warning"
              >{{ dutyCountMap[row.id]?.month || 0 }} 天</el-tag
            >
          </template>
        </el-table-column>
        <el-table-column label="本年值班" width="110" align="center">
          <template #default="{ row }">
            <el-tag type="success"
              >{{ dutyCountMap[row.id]?.year || 0 }} 天</el-tag
            >
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              :icon="Edit"
              @click="handleEdit(row)"
            >
              修改
            </el-button>
            <el-button
              type="success"
              link
              :icon="Document"
              @click="handleShowLog(row)"
            >
              日志
            </el-button>
            <el-button
              type="danger"
              link
              :icon="Delete"
              @click="doDelete(row.id)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-section" v-show="!loading">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total || 0"
          :disabled="loading"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          background
          style="width: 100%"
        />
      </div>
    </el-card>

    <!-- 添加/编辑用户对话框 -->
    <el-dialog
      v-model="showUserDialog"
      :title="isEdit ? '修改用户' : '添加用户'"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="userFormRef"
        :model="userForm"
        :rules="userFormRules"
        label-width="100px"
      >
        <el-form-item label="账号" prop="userAccount" v-if="!isEdit">
          <el-input
            v-model="userForm.userAccount"
            placeholder="请输入账号"
            :disabled="isEdit"
          />
        </el-form-item>
        <el-form-item label="用户名" prop="userName">
          <el-input v-model="userForm.userName" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone" v-if="!isEdit">
          <el-input v-model="userForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="部门" prop="dept" v-if="!isEdit">
          <el-input v-model="userForm.dept" placeholder="请输入部门" />
        </el-form-item>
        <el-form-item label="用户角色" prop="userRole">
          <el-select
            v-model="userForm.userRole"
            placeholder="请选择用户角色"
            style="width: 100%"
          >
            <el-option label="普通用户" value="user" />
            <el-option label="管理员" value="admin" />
          </el-select>
        </el-form-item>
        <el-alert
          v-if="!isEdit"
          title="提示"
          type="info"
          :closable="false"
          style="margin-bottom: 20px"
        >
          新用户的默认密码为：123456789
        </el-alert>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showUserDialog = false">取消</el-button>
          <el-button
            type="primary"
            :loading="submitting"
            @click="handleSubmitUser"
          >
            {{ isEdit ? "保存" : "添加" }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 值班日志对话框 -->
    <el-dialog
      v-model="showLogDialog"
      :title="`${currentLogUser?.userName || '用户'} 的值班日志`"
      width="800px"
      :close-on-click-modal="false"
    >
      <div class="log-calendar-container">
        <el-calendar v-model="logCalendarDate">
          <template #date-cell="{ data }">
            <div
              class="calendar-day"
              :class="{
                'duty-day': isDutyDay(data.day),
                today: isToday(data.day),
              }"
              @click="handleDateClick(data.day)"
              style="cursor: pointer"
            >
              <div class="day-number">
                {{ data.day.split("-").slice(-1)[0] }}
              </div>
              <div v-if="isDutyDay(data.day)" class="duty-badge">值班</div>
            </div>
          </template>
        </el-calendar>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showLogDialog = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 值班备注详情对话框 -->
    <el-dialog v-model="showRemarkDialog" title="值班备注" width="500px">
      <div v-if="currentRemark">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="值班日期">{{
            currentRemarkDate
          }}</el-descriptions-item>
          <el-descriptions-item label="调班备注">
            <el-text>{{ currentRemark }}</el-text>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <el-empty v-else description="该日期没有值班记录或备注" />
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showRemarkDialog = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  Search,
  Refresh,
  Delete,
  Edit,
  Plus,
  Document,
} from "@element-plus/icons-vue";
import { useRouter } from "vue-router";
import {
  listUserVOByPage,
  deleteUser,
  getUserById,
  addUser,
  updateUser,
  getDutyCount as getDutyCountApi,
  getDutyRecordsByUserId,
} from "@/api/index";
import { useLoginUserStore } from "@/stores/useLoginUserStore";

const router = useRouter();
const loginUserStore = useLoginUserStore();

// 初始化用户信息到 store（如果还没有）
const initUserInfo = () => {
  try {
    const userInfo = localStorage.getItem("userInfo");
    if (userInfo) {
      const user = JSON.parse(userInfo);
      // 如果 store 中没有用户信息，从 localStorage 同步
      if (!loginUserStore.loginUser || !loginUserStore.loginUser.id) {
        loginUserStore.setLoginUser(user);
      }
    }
  } catch (error) {
    console.error("初始化用户信息失败:", error);
  }
};

// 判断是否为管理员
const isAdmin = computed(() => {
  // 优先从 store 获取
  if (
    loginUserStore.loginUser &&
    loginUserStore.loginUser.userRole === "admin"
  ) {
    return true;
  }
  // 如果 store 中没有，从 localStorage 获取
  try {
    const userInfo = localStorage.getItem("userInfo");
    if (userInfo) {
      const user = JSON.parse(userInfo);
      return user.userRole === "admin";
    }
  } catch (error) {
    console.error("获取用户信息失败:", error);
  }
  return false;
});

// 响应式数据
const loading = ref(false);
const dataList = ref([]);
const total = ref(0);
const pageReady = ref(false);

// 调试：监听 total 变化
watch(
  total,
  (newVal) => {
    console.log("total 值变化:", newVal);
  },
  { immediate: true }
);

// 分页参数（使用 ref 以便 v-model 正确绑定）
const currentPage = ref(1);
const pageSize = ref(10);

// 用户对话框相关
const showUserDialog = ref(false);
const isEdit = ref(false);
const submitting = ref(false);
const userFormRef = ref(null);
const userForm = reactive({
  id: null,
  userAccount: "",
  userName: "",
  phone: "",
  dept: "",
  userRole: "user",
});

// 用户表单验证规则
const userFormRules = {
  userAccount: [
    { required: true, message: "请输入账号", trigger: "blur" },
    { min: 4, max: 20, message: "账号长度在 4 到 20 个字符", trigger: "blur" },
  ],
  userName: [
    { required: true, message: "请输入用户名", trigger: "blur" },
    {
      min: 2,
      max: 20,
      message: "用户名长度在 2 到 20 个字符",
      trigger: "blur",
    },
  ],
  phone: [
    { required: true, message: "请输入手机号", trigger: "blur" },
    {
      pattern: /^1[3-9]\d{9}$/,
      message: "请输入正确的手机号",
      trigger: "blur",
    },
  ],
  dept: [{ required: true, message: "请输入部门", trigger: "blur" }],
  userRole: [{ required: true, message: "请选择用户角色", trigger: "change" }],
};

// 搜索条件
const searchParams = reactive({
  current: 1,
  pageSize: 10,
  userAccount: "",
  userName: "",
});

// 值班配置（从 localStorage 读取）
const dutyConfig = ref({
  weekdayDutyList: [],
  saturdayGroup1: [],
  saturdayGroup2: [],
  monthEndDutyList: [],
  baseDate: null,
});

// 格式化日期
const formatDate = (dateValue) => {
  if (!dateValue) return "-";

  try {
    let date;
    // 处理不同的日期格式
    if (dateValue instanceof Date) {
      date = dateValue;
    } else if (typeof dateValue === "string" || typeof dateValue === "number") {
      date = new Date(dateValue);
    } else {
      return "-";
    }

    // 检查日期是否有效
    if (isNaN(date.getTime())) {
      console.warn("无效的日期值:", dateValue);
      return "-";
    }

    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");
    const hours = String(date.getHours()).padStart(2, "0");
    const minutes = String(date.getMinutes()).padStart(2, "0");
    const seconds = String(date.getSeconds()).padStart(2, "0");
    return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
  } catch (error) {
    console.error("格式化日期失败:", error, dateValue);
    return "-";
  }
};

// 值班天数映射表
const dutyCountMap = ref({});

// 日志对话框相关
const showLogDialog = ref(false);
const currentLogUser = ref(null);
const logCalendarDate = ref(new Date());
const dutyRecordDates = ref([]);
const dutyRecordsMap = ref({});

// 备注对话框相关
const showRemarkDialog = ref(false);
const currentRemark = ref("");
const currentRemarkDate = ref("");

// 批量获取值班天数（从后端接口）
const loadDutyCounts = async (userList) => {
  if (!userList || userList.length === 0) return;

  // 批量获取所有用户的值班天数
  const promises = [];
  for (const user of userList) {
    promises.push(
      getDutyCountApi({ userId: user.id, type: "month" }).then((res) => ({
        userId: user.id,
        type: "month",
        count: res.code === 0 ? res.data || 0 : 0,
      }))
    );
    promises.push(
      getDutyCountApi({ userId: user.id, type: "year" }).then((res) => ({
        userId: user.id,
        type: "year",
        count: res.code === 0 ? res.data || 0 : 0,
      }))
    );
  }

  try {
    const results = await Promise.all(promises);
    const newMap = {};
    for (const result of results) {
      if (!newMap[result.userId]) {
        newMap[result.userId] = {};
      }
      newMap[result.userId][result.type] = result.count;
    }
    dutyCountMap.value = { ...dutyCountMap.value, ...newMap };
  } catch (error) {
    console.error("批量获取值班天数失败:", error);
  }
};

// 获取数据
const fetchData = async () => {
  loading.value = true;
  try {
    // 同步分页参数
    searchParams.current = currentPage.value;
    searchParams.pageSize = pageSize.value;

    const res = await listUserVOByPage({
      current: currentPage.value,
      pageSize: pageSize.value,
      userAccount: searchParams.userAccount || undefined,
      userName: searchParams.userName || undefined,
    });

    if (res.code === 0 && res.data) {
      dataList.value = res.data.records || [];
      // 确保 total 是数字类型
      const totalValue = Number(res.data.total) || 0;
      total.value = totalValue;

      // 加载值班天数
      await loadDutyCounts(dataList.value);

      // 调试：输出分页信息
      console.log("分页信息:", {
        current: currentPage.value,
        pageSize: pageSize.value,
        total: total.value,
        totalType: typeof total.value,
        records: dataList.value.length,
        responseData: res.data,
      });

      // 同步到 searchParams
      searchParams.current = currentPage.value;
      searchParams.pageSize = pageSize.value;

      // 如果后端返回的 UserVO 没有 phone 和 dept，尝试获取完整信息
      // 使用 Promise.all 并行获取，提高性能
      const promises = dataList.value
        .filter((user) => !user.phone || !user.dept)
        .map(async (user) => {
          try {
            const fullInfo = await getUserById(user.id);
            if (fullInfo.code === 0 && fullInfo.data) {
              user.phone = fullInfo.data.phone || user.phone;
              user.dept = fullInfo.data.dept || user.dept;
            }
          } catch (error) {
            console.error(`获取用户 ${user.id} 完整信息失败:`, error);
          }
        });

      // 并行获取所有用户的完整信息
      if (promises.length > 0) {
        await Promise.all(promises);
      }
    } else {
      ElMessage.error("获取数据失败，" + (res.message || "请重试"));
    }
  } catch (error) {
    console.error("获取用户列表失败:", error);
    // 如果是权限错误，不显示错误提示（因为已经有权限检查）
    if (
      error.response?.status === 403 ||
      error.response?.data?.code === 40300
    ) {
      console.warn("权限不足，无法获取用户列表");
      router.push("/home");
    } else {
      ElMessage.error("获取用户列表失败");
    }
  } finally {
    loading.value = false;
  }
};

// 搜索处理
const doSearch = () => {
  currentPage.value = 1;
  searchParams.current = 1;
  fetchData();
};

// 重置搜索
const resetSearch = () => {
  searchParams.userAccount = "";
  searchParams.userName = "";
  currentPage.value = 1;
  searchParams.current = 1;
  fetchData();
};

// 分页参数计算属性（兼容示例代码风格）
const pagination = computed(() => {
  return {
    current: currentPage.value ?? 1,
    pageSize: pageSize.value ?? 10,
    total: total.value,
    showSizeChanger: true,
    showTotal: (total) => `共 ${total} 条`,
  };
});

// 表格变化处理（兼容示例代码风格）
const doTableChange = (page) => {
  if (page.current !== undefined) {
    currentPage.value = page.current;
    searchParams.current = page.current;
  }
  if (page.pageSize !== undefined) {
    pageSize.value = page.pageSize;
    searchParams.pageSize = page.pageSize;
    // 改变每页数量时重置到第一页
    if (page.pageSize !== pageSize.value) {
      currentPage.value = 1;
      searchParams.current = 1;
    }
  }
  fetchData();
};

// 分页大小变化
const handleSizeChange = (size) => {
  pageSize.value = size;
  searchParams.pageSize = size;
  currentPage.value = 1; // 改变每页数量时重置到第一页
  searchParams.current = 1;
  fetchData();
};

// 当前页变化
const handleCurrentChange = (current) => {
  currentPage.value = current;
  searchParams.current = current;
  fetchData();
};

// 重置用户表单
const resetUserForm = () => {
  Object.assign(userForm, {
    id: null,
    userAccount: "",
    userName: "",
    phone: "",
    dept: "",
    userRole: "user",
  });
  if (userFormRef.value) {
    userFormRef.value.clearValidate();
  }
};

// 添加用户
const handleAdd = () => {
  isEdit.value = false;
  resetUserForm();
  showUserDialog.value = true;
};

// 修改用户
const handleEdit = async (row) => {
  isEdit.value = true;
  resetUserForm();

  try {
    // 获取完整用户信息
    const res = await getUserById(row.id);
    if (res.code === 0 && res.data) {
      Object.assign(userForm, {
        id: res.data.id,
        userName: res.data.userName || "",
        userRole: res.data.userRole || "user",
      });
      // 编辑时不能修改账号、手机号、部门（根据后端 UserUpdateRequest 只有 id, userName, userRole）
    } else {
      ElMessage.error("获取用户信息失败");
      return;
    }
  } catch (error) {
    console.error("获取用户信息失败:", error);
    ElMessage.error("获取用户信息失败");
    return;
  }

  showUserDialog.value = true;
};

// 提交用户表单
const handleSubmitUser = async () => {
  if (!userFormRef.value) return;

  await userFormRef.value.validate(async (valid) => {
    if (!valid) return;

    submitting.value = true;
    try {
      if (isEdit.value) {
        // 修改用户
        const res = await updateUser({
          id: userForm.id,
          userName: userForm.userName,
          userRole: userForm.userRole,
        });

        if (res.code === 0) {
          ElMessage.success("修改用户成功");
          showUserDialog.value = false;
          fetchData(); // 刷新列表
        } else {
          ElMessage.error("修改用户失败，" + (res.message || "请重试"));
        }
      } else {
        // 添加用户
        const res = await addUser({
          userAccount: userForm.userAccount,
          userName: userForm.userName,
          phone: userForm.phone,
          dept: userForm.dept,
          userRole: userForm.userRole,
        });

        if (res.code === 0) {
          ElMessage.success("添加用户成功，默认密码为：123456789");
          showUserDialog.value = false;
          fetchData(); // 刷新列表
        } else {
          ElMessage.error("添加用户失败，" + (res.message || "请重试"));
        }
      }
    } catch (error) {
      console.error("提交用户表单失败:", error);
      ElMessage.error(isEdit.value ? "修改用户失败" : "添加用户失败");
    } finally {
      submitting.value = false;
    }
  });
};

// 删除用户
const doDelete = async (id) => {
  if (!id) {
    return;
  }

  try {
    await ElMessageBox.confirm("确定要删除该用户吗？删除后无法恢复！", "提示", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    });

    const res = await deleteUser({ id });
    if (res.code === 0) {
      ElMessage.success("删除成功");

      // 如果当前页删除后没有数据了，且不是第一页，则跳转到上一页
      if (dataList.value.length === 1 && currentPage.value > 1) {
        currentPage.value--;
        searchParams.current = currentPage.value;
      }

      // 刷新数据
      fetchData();
    } else {
      ElMessage.error("删除失败，" + (res.message || "请重试"));
    }
  } catch (error) {
    if (error !== "cancel") {
      console.error("删除用户失败:", error);
      ElMessage.error("删除失败");
    }
  }
};

// 查看值班日志
const handleShowLog = async (row) => {
  currentLogUser.value = row;
  logCalendarDate.value = new Date();
  dutyRecordDates.value = [];
  dutyRecordsMap.value = {};
  showLogDialog.value = true;

  try {
    const res = await getDutyRecordsByUserId(row.id);
    if (res.code === 0 && res.data) {
      // 过滤出已经发生的值班日期（不包括未来的日期）
      const today = new Date();
      today.setHours(0, 0, 0, 0);

      const recordsMap = {};
      const dates = [];

      res.data.forEach((record) => {
        const dutyDate = new Date(record.dutyDate);
        dutyDate.setHours(0, 0, 0, 0);

        if (dutyDate < today) {
          // 只显示今天之前的日期
          const dateStr = `${dutyDate.getFullYear()}-${String(
            dutyDate.getMonth() + 1
          ).padStart(2, "0")}-${String(dutyDate.getDate()).padStart(2, "0")}`;
          dates.push(dateStr);
          recordsMap[dateStr] = record;
        }
      });

      dutyRecordDates.value = dates;
      dutyRecordsMap.value = recordsMap;
    } else {
      ElMessage.error("获取值班记录失败");
    }
  } catch (error) {
    console.error("获取值班记录失败:", error);
    ElMessage.error("获取值班记录失败");
  }
};

// 处理日期点击
const handleDateClick = (dateStr) => {
  const record = dutyRecordsMap.value[dateStr];
  if (record) {
    currentRemarkDate.value = dateStr;
    currentRemark.value = record.remark || "无备注";
    showRemarkDialog.value = true;
  } else {
    ElMessage.info("该日期没有值班记录");
  }
};

// 判断是否为值班日期
const isDutyDay = (dateStr) => {
  return dutyRecordDates.value.includes(dateStr);
};

// 判断是否为今天
const isToday = (dateStr) => {
  const today = new Date();
  const todayStr = `${today.getFullYear()}-${String(
    today.getMonth() + 1
  ).padStart(2, "0")}-${String(today.getDate()).padStart(2, "0")}`;
  return dateStr === todayStr;
};

// 页面加载时请求数据
onMounted(() => {
  // 先初始化用户信息
  initUserInfo();

  // 延迟检查权限，确保用户信息已初始化
  setTimeout(() => {
    // 检查是否为管理员
    if (!isAdmin.value) {
      ElMessage.warning("您没有权限访问此页面");
      router.push("/home");
      return;
    }
    // 权限检查通过，显示页面
    pageReady.value = true;
    fetchData();
  }, 100);
});
</script>

<style scoped>
.user-manage-page {
  width: 100%;
  max-width: 1600px;
  margin: 0 auto;
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-title {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.search-card {
  margin-bottom: 20px;
}

.table-card {
  margin-bottom: 20px;
}

.pagination-section {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-top: 24px;
  padding-top: 16px;
  padding-bottom: 16px;
  border-top: 1px solid #e4e7ed;
  min-height: 60px;
  width: 100%;
  visibility: visible;
  opacity: 1;
}

.pagination-section :deep(.el-pagination) {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  flex-wrap: wrap;
}

.pagination-section :deep(.el-pagination__total) {
  margin-right: 16px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .user-manage-page {
    padding: 16px;
  }

  .page-title {
    font-size: 20px;
  }
}

/* 日志日历样式 */
.log-calendar-container {
  width: 100%;
}

.calendar-day {
  width: 100%;
  height: 60px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: relative;
  border-radius: 4px;
  transition: all 0.3s;
}

.calendar-day .day-number {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.calendar-day.today {
  background-color: #ecf5ff;
  border: 1px solid #409eff;
}

.calendar-day.duty-day {
  background-color: #fff3e0;
  border: 1px solid #ff9800;
}

.calendar-day.duty-day .day-number {
  color: #ff9800;
  font-weight: 600;
}

.calendar-day .duty-badge {
  font-size: 12px;
  color: #ff9800;
  background-color: #fff3e0;
  padding: 2px 6px;
  border-radius: 10px;
  margin-top: 4px;
  font-weight: 500;
}

.calendar-day:hover {
  transform: scale(1.05);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

/* 自定义日历样式 */
:deep(.el-calendar-table .el-calendar-day) {
  padding: 4px;
  height: 80px;
}

:deep(.el-calendar-table thead th) {
  padding: 12px 0;
  font-weight: 600;
  color: #303133;
}

:deep(.el-calendar__header) {
  padding: 16px;
  border-bottom: 1px solid #e4e7ed;
}

:deep(.el-calendar__title) {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}
</style>
