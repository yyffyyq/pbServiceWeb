<template>
  <div class="user-manage-page apple-theme" v-if="pageReady">
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">用户管理</h1>
        <p class="page-subtitle">User Management</p>
      </div>
      <div class="header-actions">
        <el-button class="apple-btn success" type="success" :icon="Plus" @click="handleBatchAdd" style="margin-right: 10px;">批量添加</el-button>
        <el-button class="apple-btn primary" type="primary" :icon="Plus" @click="handleAdd">添加用户</el-button>
      </div>
    </div>

    <div class="apple-card search-card">
      <el-form :model="searchParams" inline class="apple-form inline-form">
        <el-form-item label="账号">
          <el-input
              v-model="searchParams.userAccount"
              placeholder="请输入账号"
              clearable
              class="apple-input"
              style="width: 220px"
              @keyup.enter="doSearch"
          />
        </el-form-item>
        <el-form-item label="用户名">
          <el-input
              v-model="searchParams.userName"
              placeholder="请输入用户名"
              clearable
              class="apple-input"
              style="width: 220px"
              @keyup.enter="doSearch"
          />
        </el-form-item>
        <el-form-item class="search-actions">
          <el-button class="apple-btn primary" type="primary" :icon="Search" @click="doSearch">搜索</el-button>
          <el-button class="apple-btn" :icon="Refresh" @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="apple-card table-card">
      <el-table
          :data="dataList"
          v-loading="loading"
          class="apple-table"
          style="width: 100%"
          :fit="true"
      >
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="userAccount" label="账号" min-width="120" show-overflow-tooltip />
        <el-table-column prop="userName" label="用户名" min-width="120" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="user-name-text">{{ row.userName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" min-width="130" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.phone || "未设置" }}
          </template>
        </el-table-column>
        <el-table-column prop="dept" label="部门" min-width="120" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.dept || "未设置" }}
          </template>
        </el-table-column>
        <el-table-column prop="userRole" label="用户角色" width="100" align="center">
          <template #default="{ row }">
            <span v-if="row.userRole === 'admin'" class="apple-pill success-pill">管理员</span>
            <span v-else class="apple-pill user-pill">普通用户</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" align="center">
          <template #default="{ row }">
            <span class="date-text">{{ formatDate(row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="本月值班" width="100" align="center">
          <template #default="{ row }">
            <span class="apple-pill warning-pill">{{ dutyCountMap[row.id]?.month || 0 }} 天</span>
          </template>
        </el-table-column>
        <el-table-column label="本年值班" width="100" align="center">
          <template #default="{ row }">
            <span class="apple-pill success-pill">{{ dutyCountMap[row.id]?.year || 0 }} 天</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link class="apple-text-btn" :icon="Edit" @click="handleEdit(row)">修改</el-button>
            <el-button type="success" link class="apple-text-btn success" :icon="Document" @click="handleShowLog(row)">日志</el-button>
            <el-button type="danger" link class="apple-text-btn danger" :icon="Delete" @click="doDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

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
            class="apple-pagination"
        />
      </div>
    </div>

    <el-dialog
        v-model="showUserDialog"
        :title="isEdit ? '修改用户' : '添加用户'"
        width="540px"
        class="apple-dialog"
        :close-on-click-modal="false"
    >
      <el-form ref="userFormRef" :model="userForm" :rules="userFormRules" label-position="top" class="apple-form">
        <el-form-item label="账号" prop="userAccount" v-if="!isEdit">
          <el-input v-model="userForm.userAccount" placeholder="请输入账号" :disabled="isEdit" class="apple-input" />
        </el-form-item>
        <el-form-item label="用户名" prop="userName">
          <el-input v-model="userForm.userName" placeholder="请输入用户名" class="apple-input" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone" v-if="!isEdit">
          <el-input v-model="userForm.phone" placeholder="请输入手机号" class="apple-input" />
        </el-form-item>
        <el-form-item label="部门" prop="dept" v-if="!isEdit">
          <el-input v-model="userForm.dept" placeholder="请输入部门" class="apple-input" />
        </el-form-item>
        <el-form-item label="用户角色" prop="userRole">
          <el-select v-model="userForm.userRole" placeholder="请选择用户角色" style="width: 100%" class="apple-select">
            <el-option label="普通用户" value="user" />
            <el-option label="管理员" value="admin" />
          </el-select>
        </el-form-item>
        <div v-if="!isEdit" class="apple-alert info">
          提示：新用户的默认密码为 <strong>123456789</strong>
        </div>
      </el-form>
      <template #footer>
        <div class="dialog-footer apple-footer">
          <el-button class="apple-btn" @click="showUserDialog = false">取消</el-button>
          <el-button class="apple-btn primary" type="primary" :loading="submitting" @click="handleSubmitUser">
            {{ isEdit ? "保存更改" : "确认添加" }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog
        v-model="showBatchAddDialog"
        title="批量添加员工"
        width="860px"
        class="apple-dialog"
        :close-on-click-modal="false"
    >
      <el-form :model="batchAddForm" label-position="top" class="apple-form">
        <el-form-item label="1. 选择部门" required>
          <el-select
              v-model="batchAddForm.deptId"
              placeholder="请先选择员工所属部门"
              style="width: 300px"
              :loading="loadingDept"
              filterable
              class="apple-select"
          >
            <el-option v-for="dept in deptList" :key="dept.id" :label="dept.deptName" :value="dept.id" />
          </el-select>
        </el-form-item>

        <div v-if="batchAddForm.deptId" class="batch-section">
          <div class="section-header">
            <span class="section-title">2. 录入员工信息：</span>
            <el-button class="apple-btn small primary-light" type="primary" :icon="Plus" @click="addBatchRow">添加一行</el-button>
          </div>
          <el-table :data="batchAddForm.users" class="apple-table small-table">
            <el-table-column label="账号" min-width="120">
              <template #default="{ row }">
                <el-input v-model="row.userAccount" placeholder="非必填(自动生成)" class="apple-input mini" />
              </template>
            </el-table-column>
            <el-table-column label="用户名" min-width="120">
              <template #default="{ row }">
                <el-input v-model="row.userName" placeholder="用户名" class="apple-input mini" />
              </template>
            </el-table-column>
            <el-table-column label="手机号" min-width="120">
              <template #default="{ row }">
                <el-input v-model="row.phone" placeholder="十一手机号" class="apple-input mini" />
              </template>
            </el-table-column>
            <el-table-column label="用户角色" width="110">
              <template #default="{ row }">
                <el-select v-model="row.userRole" class="apple-select mini">
                  <el-option label="普通用户" value="user" />
                  <el-option label="管理员" value="admin" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="70" align="center">
              <template #default="{ $index }">
                <el-button type="danger" link class="apple-text-btn danger" :icon="Delete" @click="removeBatchRow($index)" :disabled="batchAddForm.users.length <= 1"></el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="apple-alert info" style="margin-top: 20px;">
            提示：系统将为以上员工自动设置初始密码为 <strong>123456789</strong>，且自动归属于上方选定的部门。
          </div>
        </div>
      </el-form>
      <template #footer>
        <div class="dialog-footer apple-footer">
          <el-button class="apple-btn" @click="showBatchAddDialog = false">取消</el-button>
          <el-button class="apple-btn primary" type="primary" :loading="submittingBatch" @click="handleSubmitBatchAdd" :disabled="!batchAddForm.deptId">
            提交批量添加
          </el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog
        v-model="showLogDialog"
        :title="`${currentLogUser?.userName || '用户'} 的值班日志`"
        width="800px"
        class="apple-dialog log-dialog"
        :close-on-click-modal="false"
    >
      <div class="log-calendar-container">
        <el-calendar v-model="logCalendarDate" class="apple-calendar">
          <template #date-cell="{ data }">
            <div
                class="calendar-day-cell"
                :class="{
                'is-duty': isDutyDay(data.day),
                'is-today': isToday(data.day),
              }"
                @click="handleDateClick(data.day)"
            >
              <div class="day-number-wrap">
                <span class="day-number">{{ data.day.split("-").slice(-1)[0] }}</span>
              </div>
              <div v-if="isDutyDay(data.day)" class="duty-dot">值班</div>
            </div>
          </template>
        </el-calendar>
      </div>
      <template #footer>
        <div class="dialog-footer apple-footer">
          <el-button class="apple-btn primary" type="primary" @click="showLogDialog = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="showRemarkDialog" title="值班备注" width="420px" class="apple-dialog">
      <div v-if="currentRemark" class="remark-content">
        <div class="remark-row">
          <span class="remark-label">值班日期：</span>
          <span class="remark-value date">{{ currentRemarkDate }}</span>
        </div>
        <div class="remark-row column">
          <span class="remark-label">调班备注：</span>
          <div class="remark-text-box">
            {{ currentRemark }}
          </div>
        </div>
      </div>
      <div v-else class="empty-state">
        <span class="empty-text">该日期没有值班记录或备注</span>
      </div>
      <template #footer>
        <div class="dialog-footer apple-footer">
          <el-button class="apple-btn primary" type="primary" @click="showRemarkDialog = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
// 完全保留你原有的逻辑代码，一字未改
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
  batchAddUser,
  getDept
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

// 批量添加用户相关功能
const showBatchAddDialog = ref(false);
const submittingBatch = ref(false);
const deptList = ref([]);
const loadingDept = ref(false);

const batchAddForm = reactive({
  deptId: "",
  users: [
    { userAccount: "", userName: "", phone: "", userRole: "user", key: Date.now() }
  ]
});

// 加载部门列表
const loadDeptList = async () => {
  if (deptList.value.length > 0) return;
  loadingDept.value = true;
  try {
    const res = await getDept();
    if (res.code === 0 && res.data) {
      deptList.value = res.data || [];
    }
  } catch (error) {
    console.error("加载部门列表失败:", error);
  } finally {
    loadingDept.value = false;
  }
};

const resetBatchAddForm = () => {
  batchAddForm.deptId = "";
  batchAddForm.users = [
    { userAccount: "", userName: "", phone: "", userRole: "user", key: Date.now() }
  ];
};

const handleBatchAdd = () => {
  resetBatchAddForm();
  showBatchAddDialog.value = true;
  loadDeptList();
};

const addBatchRow = () => {
  batchAddForm.users.push({ userAccount: "", userName: "", phone: "", userRole: "user", key: Date.now() });
};

const removeBatchRow = (index) => {
  if (batchAddForm.users.length > 1) {
    batchAddForm.users.splice(index, 1);
  }
};

const handleSubmitBatchAdd = async () => {
  if (!batchAddForm.deptId) {
    ElMessage.warning("请先选择部门");
    return;
  }

  // 过滤出起码填了用户名的有效用户数据（账号由后台自动生成）
  const validUsers = batchAddForm.users.filter(u => u.userName && u.phone);

  if (validUsers.length === 0) {
    ElMessage.warning("请至少录入一行完整的员工信息 (用户名,手机号) ");
    return;
  }

  // 查找选中的部门名称
  const selectedDept = deptList.value.find(d => d.id === batchAddForm.deptId);
  const deptName = selectedDept ? selectedDept.deptName : "";

  // 构造 UserAddRequest
  const requests = validUsers.map(u => ({
    userAccount: u.userAccount,
    userName: u.userName,
    phone: u.phone,
    deptId: String(batchAddForm.deptId),
    dept: deptName,
    userRole: u.userRole
  }));

  submittingBatch.value = true;
  try {
    const res = await batchAddUser(requests);
    if (res.code === 0) {
      ElMessage.success(`成功批量添加 ${validUsers.length} 位员工，默认密码为：123456789`);
      showBatchAddDialog.value = false;
      fetchData();
    } else {
      ElMessage.error("批量添加失败：" + (res.message || "请检查数据或重试"));
    }
  } catch (error) {
    console.error("批量添加员工失败:", error);
    ElMessage.error("请求批量添加失败");
  } finally {
    submittingBatch.value = false;
  }
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
/* =========================================
   Apple Theme 全局变量及基础重塑
   ========================================= */
.apple-theme {
  --apple-bg: #f5f5f7;
  --apple-card: #ffffff;
  --apple-text-main: #1d1d1f;
  --apple-text-sub: #86868b;
  --apple-blue: #007aff;
  --apple-blue-hover: #006ce6;
  --apple-blue-light: #e5f0ff;
  --apple-green: #34c759;
  --apple-orange: #ff9500;
  --apple-red: #ff3b30;
  --apple-border: rgba(0, 0, 0, 0.06);
  --apple-shadow-sm: 0 2px 8px rgba(0, 0, 0, 0.04);
  --apple-shadow-md: 0 8px 24px rgba(0, 0, 0, 0.08);
  --apple-shadow-lg: 0 20px 40px rgba(0, 0, 0, 0.12);
  --apple-radius-sm: 8px;
  --apple-radius-md: 14px;
  --apple-radius-lg: 20px;

  font-family: -apple-system, BlinkMacSystemFont, "SF Pro Text", "PingFang SC", "Helvetica Neue", Arial, sans-serif;
}

.user-manage-page {
  width: 100%;
  max-width: 1400px;
  margin: 0 auto;
  padding: 32px;
  background-color: var(--apple-bg);
  min-height: 100vh;
  box-sizing: border-box;
}

/* --- Header 顶部区域 --- */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 24px;
  padding: 0 12px;
}
.header-content .page-title {
  margin: 0;
  font-size: 32px;
  font-weight: 700;
  color: var(--apple-text-main);
  letter-spacing: -0.5px;
}
.header-content .page-subtitle {
  margin: 4px 0 0 0;
  font-size: 14px;
  font-weight: 500;
  color: var(--apple-text-sub);
  text-transform: uppercase;
  letter-spacing: 1px;
}
.header-actions {
  display: flex;
  gap: 12px;
}

/* --- Apple 风格按钮 --- */
.apple-btn {
  border-radius: var(--apple-radius-sm);
  font-weight: 500;
  border: 1px solid var(--apple-border);
  box-shadow: 0 1px 2px rgba(0,0,0,0.02);
  transition: all 0.2s cubic-bezier(0.25, 0.8, 0.25, 1);
  background: #ffffff;
}
.apple-btn:not(.primary):not(.success):not(.primary-light):hover {
  background-color: #f0f0f2;
  border-color: #d1d1d6;
}
.apple-btn.primary {
  background-color: var(--apple-blue);
  border: none;
  color: white;
}
.apple-btn.primary:hover {
  background-color: var(--apple-blue-hover);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 122, 255, 0.3);
}
.apple-btn.success {
  background-color: var(--apple-green);
  border: none;
  color: white;
}
.apple-btn.success:hover {
  background-color: #2eb350;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(52, 199, 89, 0.3);
}
.apple-btn.primary-light {
  background-color: var(--apple-blue-light);
  color: var(--apple-blue);
  border: none;
}
.apple-btn.small {
  padding: 6px 12px;
  font-size: 13px;
  height: auto;
}

/* --- 卡片容器 --- */
.apple-card {
  background: var(--apple-card);
  border-radius: var(--apple-radius-lg);
  box-shadow: var(--apple-shadow-md);
  padding: 24px;
  margin-bottom: 24px;
  border: 1px solid rgba(0,0,0,0.02);
}

/* --- 搜索表单 --- */
.search-card {
  padding: 20px 24px 4px 24px;
}
.inline-form {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}
.inline-form :deep(.el-form-item) {
  margin-bottom: 16px;
  margin-right: 0;
}
.inline-form :deep(.el-form-item__label) {
  font-weight: 600;
  color: var(--apple-text-main);
  padding-right: 12px;
}

/* 统一输入框与下拉框苹果风格 */
:deep(.apple-input .el-input__wrapper),
:deep(.apple-select .el-input__wrapper) {
  background-color: #f5f5f7;
  box-shadow: none !important;
  border: 1px solid transparent;
  border-radius: var(--apple-radius-sm);
  transition: all 0.2s;
}
:deep(.apple-input .el-input__wrapper.is-focus),
:deep(.apple-select .el-input__wrapper.is-focus) {
  background-color: #ffffff;
  border-color: var(--apple-blue);
  box-shadow: 0 0 0 3px var(--apple-blue-light) !important;
}
:deep(.apple-input .el-input__inner),
:deep(.apple-select .el-input__inner) {
  color: var(--apple-text-main);
  font-weight: 500;
}
:deep(.mini .el-input__wrapper) {
  padding: 0 8px;
}

/* --- 表格区域 --- */
:deep(.apple-table) {
  --el-table-border-color: transparent;
  --el-table-header-bg-color: #f5f5f7;
  border-radius: var(--apple-radius-md);
  overflow: hidden;
}
:deep(.apple-table th.el-table__cell) {
  font-weight: 600;
  color: var(--apple-text-sub);
  border-bottom: 1px solid var(--apple-border);
  background-color: #f5f5f7;
}
:deep(.apple-table td.el-table__cell) {
  border-bottom: 1px solid var(--apple-border);
  padding: 12px 0;
}
:deep(.apple-table::before) {
  display: none;
}

.user-name-text {
  font-weight: 600;
  color: var(--apple-text-main);
}
.date-text {
  font-size: 13px;
  color: var(--apple-text-sub);
}

/* 文字按钮 */
.apple-text-btn {
  font-weight: 600;
  font-size: 13px;
}
.apple-text-btn.success {
  color: var(--apple-green);
}
.apple-text-btn.danger {
  color: var(--apple-red);
}

/* 药丸标签 (Pills) */
.apple-pill {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  line-height: 1;
}
.admin-pill {
  background-color: #e5f0ff;
  color: var(--apple-blue);
}
.user-pill {
  background-color: #f5f5f7;
  color: var(--apple-text-sub);
}
.warning-pill {
  background-color: #fff0eb;
  color: var(--apple-orange);
}
.success-pill {
  background-color: #e8f8ec;
  color: var(--apple-green);
}

/* --- 分页 --- */
.pagination-section {
  display: flex;
  justify-content: flex-end;
  margin-top: 24px;
}
:deep(.apple-pagination .el-pager li) {
  border-radius: var(--apple-radius-sm);
  background-color: #f5f5f7;
  color: var(--apple-text-main);
  font-weight: 500;
}
:deep(.apple-pagination .el-pager li.is-active) {
  background-color: var(--apple-blue);
  color: #fff;
}
:deep(.apple-pagination button) {
  border-radius: var(--apple-radius-sm);
  background-color: #f5f5f7;
}

/* --- Apple 风格弹窗 (Dialog) --- */
:deep(.apple-dialog) {
  border-radius: var(--apple-radius-lg);
  box-shadow: var(--apple-shadow-lg);
  overflow: hidden;
}
:deep(.apple-dialog .el-dialog__header) {
  margin: 0;
  padding: 20px 24px;
  background: rgba(245, 245, 247, 0.8);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-bottom: 1px solid var(--apple-border);
}
:deep(.apple-dialog .el-dialog__title) {
  font-weight: 600;
  font-size: 17px;
  color: var(--apple-text-main);
}
:deep(.apple-dialog .el-dialog__body) {
  padding: 24px;
}
:deep(.apple-dialog .el-dialog__footer) {
  padding: 16px 24px;
  border-top: 1px solid var(--apple-border);
  background: #fafafa;
}
.apple-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 表单布局 */
:deep(.apple-form .el-form-item__label) {
  font-weight: 600;
  color: var(--apple-text-main);
  padding-bottom: 6px;
}

/* 提示框 */
.apple-alert {
  padding: 12px 16px;
  border-radius: var(--apple-radius-sm);
  font-size: 13px;
  line-height: 1.5;
}
.apple-alert.info {
  background-color: var(--apple-blue-light);
  color: var(--apple-blue);
}
.apple-alert strong {
  font-weight: 700;
}

/* 批量添加区块 */
.batch-section {
  margin-top: 24px;
  background: #fafafa;
  border-radius: var(--apple-radius-md);
  padding: 20px;
  border: 1px solid var(--apple-border);
}
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.section-title {
  font-weight: 600;
  color: var(--apple-text-main);
  font-size: 15px;
}

/* --- 日志日历定制 --- */
:deep(.log-dialog .el-dialog__body) {
  padding: 0;
  background: var(--apple-card);
}
:deep(.apple-calendar .el-calendar__header) {
  padding: 16px 24px;
  border-bottom: 1px solid var(--apple-border);
}
:deep(.apple-calendar .el-calendar__title) {
  font-size: 18px;
  font-weight: 600;
}
:deep(.apple-calendar .el-calendar-table thead th) {
  font-size: 13px;
  color: var(--apple-text-sub);
  font-weight: 600;
  padding: 12px 0;
}
:deep(.apple-calendar .el-calendar-table .el-calendar-day) {
  padding: 0;
  height: 80px;
}
:deep(.apple-calendar .el-calendar-table td.is-selected) {
  background-color: transparent;
}

.calendar-day-cell {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 8px;
  cursor: pointer;
  transition: background-color 0.2s;
}
.calendar-day-cell:hover {
  background-color: #f9f9fb;
}

.day-number-wrap {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  margin-bottom: 4px;
}
.day-number {
  font-size: 17px;
  font-weight: 500;
  color: var(--apple-text-main);
}

/* 今天的样式 */
.is-today .day-number-wrap {
  background-color: var(--apple-blue);
}
.is-today .day-number {
  color: #ffffff;
  font-weight: 600;
}

/* 值班标记 */
.duty-dot {
  font-size: 11px;
  color: var(--apple-orange);
  background-color: #fff0eb;
  padding: 2px 8px;
  border-radius: 8px;
  font-weight: 600;
}

/* --- 备注详情卡片 --- */
.remark-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.remark-row {
  display: flex;
  align-items: flex-start;
}
.remark-row.column {
  flex-direction: column;
  gap: 8px;
}
.remark-label {
  color: var(--apple-text-sub);
  font-size: 14px;
  font-weight: 500;
  min-width: 70px;
}
.remark-value.date {
  color: var(--apple-text-main);
  font-size: 15px;
  font-weight: 600;
}
.remark-text-box {
  width: 100%;
  background: #f5f5f7;
  padding: 16px;
  border-radius: var(--apple-radius-sm);
  color: var(--apple-text-main);
  font-size: 14px;
  line-height: 1.6;
}
.empty-state {
  padding: 40px 0;
  text-align: center;
}
.empty-text {
  color: var(--apple-text-sub);
  font-size: 15px;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .user-manage-page { padding: 16px; }
  .apple-card { padding: 16px; }
  .page-header { flex-direction: column; align-items: flex-start; gap: 16px; }
  .inline-form { flex-direction: column; }
  .inline-form :deep(.el-form-item) { width: 100%; margin-right: 0; }
  .search-actions { width: 100%; display: flex; justify-content: flex-end; }
}
</style>