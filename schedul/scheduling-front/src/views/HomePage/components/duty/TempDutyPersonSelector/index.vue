<template>
  <el-dialog
      v-model="visible"
      title="批量添加临时排班"
      width="800px"
      @closed="handleClosed"
      :close-on-click-modal="false"
      append-to-body
      class="apple-dialog"
  >
    <div class="selector-container">
      <div class="top-actions">
        <div class="date-badge">
          排班日期：<span class="highlight">{{ targetDate }}</span>
        </div>
        <el-input
            v-model="searchKeyword"
            placeholder="在当前部门搜索姓名..."
            clearable
            prefix-icon="Search"
            class="apple-search-input"
            style="width: 240px;"
        />
      </div>

      <div class="split-layout">
        <div class="dept-sidebar" v-loading="loadingDept">
          <div
              v-for="dept in deptList"
              :key="dept.id"
              class="dept-item"
              :class="{ 'active': activeDeptId === dept.id }"
              @click="handleDeptClick(dept.id)"
          >
            {{ dept.deptName }}
          </div>
        </div>

        <div class="user-content">
          <el-table
              ref="tableRef"
              :data="filteredPersons"
              height="320"
              style="width: 100%"
              class="apple-table"
              row-key="id"
              @selection-change="handleSelectionChange"
              v-loading="loadingUser"
          >
            <el-table-column type="selection" :reserve-selection="true" width="55" align="center" />
            <el-table-column prop="userName" label="姓名" width="120" />
            <el-table-column prop="userAccount" label="账号/工号" />
            <el-table-column prop="userRole" label="角色" width="100">
              <template #default="{ row }">
                <span class="role-tag">{{ row.userRole === 'admin' ? '管理员' : '普通用户' }}</span>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <div class="selection-info">
        已跨部门选择 <span class="highlight-count">{{ selectedIds.length }}</span> 人
      </div>

      <div class="remark-form">
        <el-form label-width="60px" class="apple-form">
          <el-form-item label="备注">
            <el-input
                v-model="remark"
                type="textarea"
                :rows="2"
                placeholder="选填，例如：代替某某值班、国庆加班等"
                class="apple-textarea"
            />
          </el-form-item>
        </el-form>
      </div>
    </div>

    <template #footer>
      <div class="apple-footer">
        <el-button class="apple-btn" @click="visible = false">取消</el-button>
        <el-button class="apple-btn primary" :loading="submitting" @click="handleSubmit">
          确认添加
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue';
import { ElMessage } from 'element-plus';
import { Search } from '@element-plus/icons-vue';
import { getDeptList, getUserByDeptId, addTempDutyBatch } from './api';

const props = defineProps({
  show: Boolean,
  targetDate: String
});

const emit = defineEmits(['update:show', 'success']);

const visible = computed({
  get: () => props.show,
  set: (val) => emit('update:show', val)
});

// 状态定义
const tableRef = ref(null);
const loadingDept = ref(false);
const loadingUser = ref(false);
const submitting = ref(false);

const deptList = ref([]);
const activeDeptId = ref(null);
const userList = ref([]); // 当前部门的员工列表

const searchKeyword = ref('');
const selectedIds = ref([]);
const remark = ref('');

// 监听弹窗打开，初始化部门数据
watch(() => props.show, async (val) => {
  if (val) {
    await loadDepts();
  }
});

// 1. 加载部门列表
const loadDepts = async () => {
  loadingDept.value = true;
  try {
    const res = await getDeptList();
    deptList.value = res.data || [];
    // 如果有部门，默认选中第一个部门并加载其员工
    if (deptList.value.length > 0) {
      handleDeptClick(deptList.value[0].id);
    }
  } catch (error) {
    console.error("加载部门失败:", error);
  } finally {
    loadingDept.value = false;
  }
};

// 2. 点击部门，加载该部门下的员工
const handleDeptClick = async (deptId) => {
  if (activeDeptId.value === deptId) return; // 避免重复点击

  activeDeptId.value = deptId;
  loadingUser.value = true;
  searchKeyword.value = ''; // 切换部门时清空搜索框

  try {
    // 传入 pageSize 一个较大的值，以拉取该部门下大部分/所有用户用于选择
    const params = { current: 1, pageSize: 500 };
    const res = await getUserByDeptId(deptId, params);
    // 根据你的接口响应结构，数据在 data.records 里
    userList.value = res.data?.records || [];
  } catch (error) {
    console.error("加载员工失败:", error);
  } finally {
    loadingUser.value = false;
  }
};

// 本地搜索过滤逻辑 (仅过滤当前右侧展示的员工)
const filteredPersons = computed(() => {
  if (!searchKeyword.value) return userList.value;
  const lowerKeyword = searchKeyword.value.toLowerCase();
  return userList.value.filter(p =>
      p.userName && p.userName.toLowerCase().includes(lowerKeyword)
  );
});

// 监听多选框变化 (跨部门保留选中)
const handleSelectionChange = (selection) => {
  // 因为开启了 reserve-selection，这里的 selection 会包含所有历史选中的行
  selectedIds.value = selection.map(row => row.id);
};

// 提交批量新增
const handleSubmit = async () => {
  if (selectedIds.value.length === 0) {
    return ElMessage.warning('请至少选择一名人员进行排班');
  }

  submitting.value = true;
  try {
    const payload = {
      baseDate: props.targetDate,
      dutyType: 'groupe_temporaire',
      remark: remark.value,
      userIdList: selectedIds.value
    };

    const res = await addTempDutyBatch(payload);

    if (res.code === 0) {
      ElMessage.success(`成功为 ${selectedIds.value.length} 人添加临时排班`);
      emit('success');
      visible.value = false;
    } else {
      ElMessage.error(res.message || '添加失败');
    }
  } catch (error) {
    console.error("提交排班失败:", error);
  } finally {
    submitting.value = false;
  }
};

// 弹窗关闭后清理残留数据
const handleClosed = () => {
  searchKeyword.value = '';
  selectedIds.value = [];
  remark.value = '';
  activeDeptId.value = null;
  userList.value = [];
  // 必须清空表格的选中状态，否则下次打开弹窗还会保留上次勾选的人
  if (tableRef.value) {
    tableRef.value.clearSelection();
  }
};
</script>

<style scoped>
.selector-container { display: flex; flex-direction: column; gap: 16px; }
.top-actions { display: flex; justify-content: space-between; align-items: center; margin-bottom: 4px; }
.date-badge { font-size: 14px; color: var(--apple-text-main); background: var(--apple-bg); padding: 6px 12px; border-radius: var(--apple-radius-sm); font-weight: 500; }
.highlight { color: var(--apple-blue); font-weight: 600; }

/* 分栏布局核心样式 */
.split-layout {
  display: flex;
  height: 320px;
  border: 1px solid var(--apple-border);
  border-radius: var(--apple-radius-sm);
  overflow: hidden;
  background: var(--apple-card);
}
.dept-sidebar {
  width: 180px;
  background: #fbfbfc;
  border-right: 1px solid var(--apple-border);
  overflow-y: auto;
}
.dept-item {
  padding: 14px 16px;
  font-size: 14px;
  color: var(--apple-text-main);
  cursor: pointer;
  transition: all 0.2s ease;
  border-left: 3px solid transparent;
}
.dept-item:hover {
  background: #f0f0f2;
}
.dept-item.active {
  background: #ffffff;
  color: var(--apple-blue);
  font-weight: 600;
  border-left-color: var(--apple-blue);
  box-shadow: inset 0 1px 0 rgba(0,0,0,0.02), inset 0 -1px 0 rgba(0,0,0,0.02);
}
.user-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.selection-info { font-size: 13px; color: var(--apple-text-sub); margin-top: -4px; }
.highlight-count { color: var(--apple-blue); font-weight: 600; font-size: 15px; padding: 0 4px; }
.remark-form { margin-top: 4px; border-top: 1px dashed var(--apple-border); padding-top: 16px;}
.role-tag { font-size: 12px; background: var(--apple-bg); padding: 2px 6px; border-radius: 4px; color: var(--apple-text-sub); }

:deep(.el-table-column--selection .cell) { padding-left: 14px; }
/* 移除表格自带的底部边框以免重叠 */
:deep(.apple-table .el-table__inner-wrapper::before) { display: none; }
</style>