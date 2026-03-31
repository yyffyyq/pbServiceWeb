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
            <el-table-column
                type="selection"
                :reserve-selection="true"
                :selectable="checkSelectable"
                width="55"
                align="center"
            />
            <el-table-column prop="userName" label="姓名" width="120" />
            <el-table-column prop="userAccount" label="账号/工号" />
            <el-table-column prop="userRole" label="角色" width="100">
              <template #default="{ row }">
                <span class="role-tag">{{ row.userRole === 'admin' ? '管理员' : '普通用户' }}</span>
              </template>
            </el-table-column>

            <el-table-column label="当天状态" width="100">
              <template #default="{ row }">
                <span v-if="row.dutyType === 'groupe_temporaire'" style="color: #67c23a; font-size: 12px;">临时组</span>
                <span v-else-if="row.dutyType" style="color: #e6a23c; font-size: 12px;">已在其他组</span>
                <span v-else style="color: #909399; font-size: 12px;">未排班</span>
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
// ----------引入接口-----
import { getDeptList, getUserByDeptId, getTempDutyAccountByDeptId, addTempDutyBatch } from './api';

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
const userList = ref([]);

const searchKeyword = ref('');
const selectedIds = ref([]);
const remark = ref('');

// --- 判断是否可以勾选的逻辑 ---
const checkSelectable = (row) => {
  // 如果当天他有值班类型，且不是临时组（说明是单双周组），则变灰不可选
  if (row.dutyType && row.dutyType !== 'groupe_temporaire') {
    return false;
  }
  return true; // 未排班的，或者已经是临时组的，允许操作
};

watch(() => props.show, async (val) => {
  if (val) {
    await loadDepts();
  }
});

const loadDepts = async () => {
  loadingDept.value = true;
  try {
    const res = await getDeptList();
    deptList.value = res.data || [];
    if (deptList.value.length > 0) {
      handleDeptClick(deptList.value[0].id);
    }
  } catch (error) {
    console.error("加载部门失败:", error);
  } finally {
    loadingDept.value = false;
  }
};

// 先获取部门信息，然后通过部门信息获取员工信息，把他组装在一起，作为主基调
// --- 双重请求 + 数据组装 ---
const handleDeptClick = async (deptId) => {
  if (activeDeptId.value === deptId) return;

  activeDeptId.value = deptId;
  loadingUser.value = true;
  searchKeyword.value = '';

  try {
    // 1. 获取部门下所有的用户（保底）
    const userRes = await getUserByDeptId(deptId, { current: 1, pageSize: 500 });
    const allUsers = userRes.data?.records || [];

    // 2. 获取该部门当天的排班状态（回显）
    const statusRes = await getTempDutyAccountByDeptId(deptId, {
      current: 1,
      pageSize: 500,
      dutyDate: props.targetDate
    });
    const statusRecords = statusRes.data?.records || [];

    // 3. 建立一个映射字典，方便找状态 (userId => dutyType)
    const statusMap = {};
    statusRecords.forEach(item => {
      // API 返回的记录里含有 userId 和 dutyType
      statusMap[item.userId] = item.dutyType;
    });

    // 4. 将状态拼接到所有用户数据中
    userList.value = allUsers.map(user => {
      return {
        ...user,
        // 如果状态表里有他，就打上 dutyType 标记，没有就是 null (未排班)
        dutyType: statusMap[user.id] || null
      };
    });

    // 5. 等待表格 DOM 更新后，执行自动打勾操作
    await nextTick();
    if (tableRef.value) {
      userList.value.forEach(row => {
        // 如果发现他今天是临时组，自动勾选
        if (row.dutyType === 'groupe_temporaire') {
          tableRef.value.toggleRowSelection(row, true);
        }
      });
    }

  } catch (error) {
    console.error("加载员工失败:", error);
  } finally {
    loadingUser.value = false;
  }
};

const filteredPersons = computed(() => {
  if (!searchKeyword.value) return userList.value;
  const lowerKeyword = searchKeyword.value.toLowerCase();
  return userList.value.filter(p =>
      p.userName && p.userName.toLowerCase().includes(lowerKeyword)
  );
});

// 因为 row-key 是 id，这里直接取 row.id
const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(row => row.id);
};

const handleSubmit = async () => {
  submitting.value = true;
  try {
    const payload = {
      baseDate: props.targetDate,
      dutyType: 'groupe_temporaire',
      remark: remark.value,
      userIdList: selectedIds.value
    };

    // ----------------------添加临时组排班接口--------------------------------
    const res = await addTempDutyBatch(payload);

    if (res.code === 0) {
      ElMessage.success(`操作成功`);
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

const handleClosed = () => {
  searchKeyword.value = '';
  selectedIds.value = [];
  remark.value = '';
  activeDeptId.value = null;
  userList.value = [];
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