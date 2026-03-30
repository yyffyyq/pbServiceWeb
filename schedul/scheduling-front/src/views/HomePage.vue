<template>
  <div class="home-page apple-theme">
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">值班安排表</h1>
        <p class="page-subtitle">Schedule Management</p>
      </div>
      <div class="header-actions" v-if="isAdmin">
        <el-button class="apple-btn" :icon="Setting" @click="showConfigDialog = true">配置管理</el-button>
        <el-button class="apple-btn primary" type="primary" :icon="DocumentAdd" @click="showGenerateDialog = true">生成记录</el-button>
      </div>
    </div>

    <div class="calendar-card">
      <div class="month-selector">
        <el-button class="apple-icon-btn" :icon="ArrowLeft" circle @click="prevMonth" />
        <div class="current-month-wrapper">
          <el-date-picker
              v-model="currentDate"
              type="month"
              placeholder="选择月份"
              format="YYYY年 MM月"
              value-format="YYYY-MM"
              @change="handleMonthChange"
              class="apple-date-picker"
              :clearable="false"
          />
        </div>
        <el-button class="apple-icon-btn" :icon="ArrowRight" circle @click="nextMonth" />
        <div class="divider"></div>
        <el-button class="apple-btn" :icon="Refresh" @click="refreshCalendar">刷新</el-button>
      </div>

      <div class="calendar-grid">
        <div class="calendar-weekdays">
          <div v-for="day in weekdays" :key="day" class="weekday-header">
            {{ day }}
          </div>
        </div>

        <div class="calendar-days">
          <div
              v-for="(day, index) in calendarDays"
              :key="index"
              class="calendar-day"
              :class="{
              'other-month': day.isOtherMonth,
              'today': day.isToday,
              'weekend': day.isWeekend,
              'holiday': day.isHoliday,
              'adjusted-workday': day.isAdjusted
            }"
              @click="handleDayClick(day)"
          >
            <div class="day-header">
              <span class="day-number-wrapper">
                <span class="day-number">{{ day.date.getDate() }}</span>
              </span>
              <span class="day-tags">
                <span v-if="day.isHoliday" class="apple-tag holiday-tag">休</span>
                <span v-if="day.isAdjusted" class="apple-tag workday-tag">班</span>
                <span v-if="day.weekType" class="apple-tag week-type-tag">{{ day.weekType }}</span>
              </span>
            </div>

            <div class="day-content">
              <template v-if="day.dutyPersons && day.dutyPersons.length > 0">
                <div class="duty-pills">
                  <div class="duty-pill" :class="{ 'is-weekend-pill': day.isWeekend }">
                    <span class="pill-name">{{ day.dutyPersons[0].userName }}</span>
                    <span v-if="day.dutyPersons.length > 1" class="pill-count">+{{ day.dutyPersons.length - 1 }}</span>
                  </div>
                </div>
                <div class="duty-details-mini">
                  <div class="detail-text">{{ day.dutyPersons[0].dept || '未设置部门' }}</div>
                </div>
              </template>
              <div v-else class="no-duty">
                <span class="no-duty-text">未安排</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <el-dialog
        v-model="showConfigDialog"
        title="值班配置管理"
        width="1000px"
        :close-on-click-modal="false"
        class="apple-dialog"
    >
      <el-tabs v-model="configTab" class="apple-tabs">
        <el-tab-pane label="工作日排班" name="weekday">
          <div class="config-section">
            <div class="section-header">
              <span class="section-desc">工作日固定值班人员，每周生效。</span>
              <el-button class="apple-btn primary" type="primary" :icon="Plus" @click="handleAddWeekdayDuty">添加人员</el-button>
            </div>
            <el-table :data="weekdayDutyList" class="apple-table" style="width: 100%; margin-top: 16px;" v-loading="loadingUsers">
              <el-table-column prop="userName" label="姓名" width="120" />
              <el-table-column prop="dept" label="部门" width="150" />
              <el-table-column prop="phone" label="手机号" width="150" />
              <el-table-column label="操作" width="150" align="center">
                <template #default="{ row, $index }">
                  <el-button type="danger" link class="apple-text-btn danger" :icon="Delete" @click="handleRemoveWeekdayDuty($index)">
                    移除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

        <el-tab-pane label="周六轮值 (单/双周)" name="saturday">
          <div class="config-section">
            <div class="section-header base-date-header">
              <div class="base-date-config">
                <span class="label">基准单周日期：</span>
                <el-date-picker
                    v-model="baseDate"
                    type="date"
                    placeholder="选择基准日期"
                    format="YYYY-MM-DD"
                    value-format="YYYY-MM-DD"
                    class="apple-date-picker"
                    style="width: 200px; margin-right: 12px;"
                    @change="handleBaseDateChange"
                />
                <span class="date-desc">设定此日期所在周为「单周」</span>
              </div>
            </div>

            <el-row :gutter="24" style="margin-top: 24px;">
              <el-col :span="12">
                <div class="apple-sub-card">
                  <div class="group-header">
                    <h4>单周组</h4>
                    <el-button class="apple-btn small primary" type="primary" :icon="Plus" @click="handleAddSaturdayDuty('group1')">添加</el-button>
                  </div>
                  <el-table :data="saturdayGroup1" class="apple-table small" style="width: 100%; margin-top: 16px;" v-loading="loadingUsers">
                    <el-table-column prop="userName" label="姓名" />
                    <el-table-column prop="dept" label="部门" />
                    <el-table-column label="操作" width="80" align="center">
                      <template #default="{ row, $index }">
                        <el-button type="danger" link class="apple-text-btn danger" @click="handleRemoveSaturdayDuty('group1', $index)">移除</el-button>
                      </template>
                    </el-table-column>
                  </el-table>
                </div>
              </el-col>
              <el-col :span="12">
                <div class="apple-sub-card">
                  <div class="group-header">
                    <h4>双周组</h4>
                    <el-button class="apple-btn small primary" type="primary" :icon="Plus" @click="handleAddSaturdayDuty('group2')">添加</el-button>
                  </div>
                  <el-table :data="saturdayGroup2" class="apple-table small" style="width: 100%; margin-top: 16px;" v-loading="loadingUsers">
                    <el-table-column prop="userName" label="姓名" />
                    <el-table-column prop="dept" label="部门" />
                    <el-table-column label="操作" width="80" align="center">
                      <template #default="{ row, $index }">
                        <el-button type="danger" link class="apple-text-btn danger" @click="handleRemoveSaturdayDuty('group2', $index)">移除</el-button>
                      </template>
                    </el-table-column>
                  </el-table>
                </div>
              </el-col>
            </el-row>
          </div>
        </el-tab-pane>

        <el-tab-pane label="月末冲刺排班" name="monthEnd">
          <div class="config-section">
            <div class="section-header">
              <span class="section-desc">每月最后两天的额外值班安排。</span>
              <el-button class="apple-btn primary" type="primary" :icon="Plus" @click="handleAddMonthEndDuty">添加人员</el-button>
            </div>
            <el-table :data="monthEndDutyList" class="apple-table" style="width: 100%; margin-top: 16px;" v-loading="loadingUsers">
              <el-table-column prop="userName" label="姓名" width="120" />
              <el-table-column prop="dept" label="部门" width="150" />
              <el-table-column prop="phone" label="手机号" width="150" />
              <el-table-column label="操作" width="150" align="center">
                <template #default="{ row, $index }">
                  <el-button type="danger" link class="apple-text-btn danger" :icon="Delete" @click="handleRemoveMonthEndDuty($index)">移除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
      </el-tabs>

      <template #footer>
        <div class="dialog-footer apple-footer">
          <el-button class="apple-btn" @click="showConfigDialog = false">取消</el-button>
          <el-button class="apple-btn primary" type="primary" @click="handleSaveConfig">保存配置</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog
        v-model="showAddPersonDialog"
        :title="addPersonDialogTitle"
        width="540px"
        class="apple-dialog"
        @close="resetPersonForm"
    >
      <el-form
          ref="personFormRef"
          :model="personForm"
          :rules="personFormRules"
          label-position="top"
          class="apple-form"
      >
        <el-form-item label="选择人员" prop="userIds">
          <el-cascader
              v-model="personForm.userIds"
              :options="cascaderOptions"
              :props="cascaderProps"
              placeholder="搜索或选择部门及人员（可多选）"
              clearable
              filterable
              collapse-tags
              collapse-tags-tooltip
              class="apple-cascader"
              style="width: 100%;"
          >
            <template #default="{ node, data }">
              <span>{{ data.label }}</span>
              <span v-if="data.account" class="cascader-account">({{ data.account }})</span>
            </template>
          </el-cascader>
        </el-form-item>

        <el-form-item label="已选择" v-if="selectedUserIds && selectedUserIds.length > 0">
          <div class="apple-selected-users">
            <div class="apple-user-tag" v-for="userId in selectedUserIds" :key="userId">
              {{ getUserNameById(userId) }}
              <span class="tag-close" @click="handleRemoveSelectedUser(userId)">×</span>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="调班备注" prop="remark">
          <el-input
              v-model="personForm.remark"
              type="textarea"
              :rows="3"
              placeholder="请输入调班原因或备注信息..."
              maxlength="200"
              show-word-limit
              class="apple-textarea"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer apple-footer">
          <el-button class="apple-btn" @click="showAddPersonDialog = false">取消</el-button>
          <el-button class="apple-btn primary" type="primary" @click="handleSubmitPerson" :loading="submittingPerson">完成</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog
        v-model="showGenerateDialog"
        title="生成值班记录"
        width="460px"
        class="apple-dialog"
        :close-on-click-modal="false"
    >
      <div class="apple-alert">
        <el-icon><Calendar /></el-icon>
        <div class="alert-content">
          <p>系统每天凌晨 02:00 会自动生成当天的记录。</p>
          <p>仅需补全历史或预生成记录时使用此功能。</p>
        </div>
      </div>
      <el-form :model="generateForm" label-position="top" class="apple-form" style="margin-top:20px;">
        <el-form-item label="开始日期">
          <el-date-picker
              v-model="generateForm.startDate"
              type="date"
              placeholder="选择开始日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              class="apple-date-picker"
              style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker
              v-model="generateForm.endDate"
              type="date"
              placeholder="选择结束日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              class="apple-date-picker"
              style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer apple-footer">
          <el-button class="apple-btn" @click="showGenerateDialog = false">取消</el-button>
          <el-button class="apple-btn primary" type="primary" @click="handleGenerateRecords" :loading="generatingRecords">立即生成</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog
        v-model="showDayDetailDialog"
        title="值班人员详情"
        width="900px"
        class="apple-dialog detail-dialog"
    >
      <div class="detail-actions">
<!--        <el-button v-if="isAdmin && selectedDayInfo.isEditable" class="apple-btn primary-light" :icon="Edit" @click="handleEditDayDuty">-->
<!--          编辑排班-->
<!--        </el-button>-->
<!--        <div v-else></div>-->
        <el-button class="apple-btn success" :icon="Download" @click="exportDetailToPng" :loading="exportLoading">
          导出 PNG
        </el-button>
      </div>

      <div ref="exportContainer" class="export-container apple-export-wrapper">
        <div class="duty-table-wrapper">
          <h2 class="apple-export-title">锦途周末值班表</h2>
          <table class="duty-export-table">
            <thead>
            <tr>
              <th class="dept-header">部门</th>
              <th class="date-header">{{ formatDateHeader(selectedDayInfo.dateStr, true) }}</th>
            </tr>
            </thead>
            <tbody>
            <template v-if="selectedDayInfo.dutyPersons && selectedDayInfo.dutyPersons.length > 0">
              <template v-for="(deptGroup, deptName) in groupByDept(selectedDayInfo.dutyPersons)" :key="deptName">
                <tr v-for="(person, index) in deptGroup" :key="person.userName">
                  <td v-if="index === 0" :rowspan="deptGroup.length" class="dept-cell">
                    {{ deptName }}
                  </td>
                  <td class="person-cell">
                    <div class="person-info-cell">
                      <span class="person-name-text">{{ person.userName }}</span>
                      <span class="person-phone-text">{{ person.phone || '未设置' }}</span>
                    </div>
                  </td>
                </tr>
              </template>
            </template>
            <tr v-else>
              <td colspan="2" class="empty-message">该日期暂无人员排班</td>
            </tr>
            </tbody>
          </table>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
// [注意]：此处的 <script setup> 逻辑与原代码 100% 保持一致，直接复用你原有的所有导入和方法逻辑。
import { ref, reactive, computed, onMounted, watch, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft,
  ArrowRight,
  Refresh,
  Setting,
  Plus,
  Delete,
  Edit,
  Calendar,
  Phone,
  Download,
  DocumentAdd
} from '@element-plus/icons-vue'
import html2canvas from 'html2canvas'
import {
  listUserVOByPage,
  getUserById,
  addUser,
  updateUser,
  getDutyConfig,
  saveBaseDate,
  addDutyPerson,
  deleteDutyPerson,
  getDutyPersonListByDate,
  deleteUser,
  generateDutyRecords,
  getDutyRecordsByDateRange,
  updateDutyConfigFrom,
  getDept,
  getUserBydeptId
} from '@/api/index'
import {
  isPublicHoliday,
  isAdjustedWorkday,
  isRestDay,
  isNormalWeekend,
  initHolidayConfig
} from '@/utils/holiday'
import { useLoginUserStore } from '@/stores/useLoginUserStore'

const loginUserStore = useLoginUserStore()
const exportContainer = ref(null)
const exportLoading = ref(false)
const currentDate = ref(new Date().toISOString().slice(0, 7))
const showConfigDialog = ref(false)
const showGenerateDialog = ref(false)
const generatingRecords = ref(false)
const generateForm = reactive({ startDate: '', endDate: '' })

const isAdmin = computed(() => {
  if (loginUserStore.loginUser && loginUserStore.loginUser.userRole === 'admin') return true
  try {
    const userInfo = localStorage.getItem('userInfo')
    if (userInfo) {
      const user = JSON.parse(userInfo)
      return user.userRole === 'admin'
    }
  } catch (error) {}
  return false
})

const showAddPersonDialog = ref(false)
const showDayDetailDialog = ref(false)
const addPersonDialogTitle = ref('添加值班人员')
const personFormRef = ref(null)
const editIndex = ref(-1)
const isEditMode = computed(() => editIndex.value >= 0)
const loadingUsers = ref(false)
const loadingUserList = ref(false)
const submittingPerson = ref(false)
const deptList = ref([])
const loadingDept = ref(false)
const allUsers = ref([])

const selectedUserIds = computed(() => Array.isArray(personForm.userIds) ? personForm.userIds : [])
const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
const configTab = ref('weekday')
const weekdayDutyList = ref([])
const saturdayGroup1 = ref([])
const saturdayGroup2 = ref([])
const monthEndDutyList = ref([])
const baseDate = ref(new Date().toISOString().slice(0, 10))
const currentSaturdayGroup = ref('group1')
const dutyRecordsCache = ref({})

const selectedDayInfo = reactive({
  dateStr: '', weekdayName: '', isWeekend: false, dutyPersons: [],
  isEditable: false, isSaturday: false, isMonthEnd: false, date: null
})

const personForm = reactive({ userIds: [], remark: '' })
const personFormRules = {
  userIds: [
    { required: true, message: '请选择用户', trigger: 'change' },
    { type: 'array', min: 1, message: '请至少选择一个用户', trigger: 'change' }
  ],
  remark: [
    { required: false, message: '请输入调班备注', trigger: 'blur' },
    { min: 2, max: 200, message: '备注长度在20到200个字符', trigger: 'blur' }
  ]
}

// 1. 优化排班冲突拦截：如果在编辑模式，就不禁用，否则连原来的人都不让选了
const isUserAlreadyConfigured = (userId) => {
  // if (dateEditMode.value)
  return false; // 编辑模式放开限制

  // return weekdayDutyList.value.some(config => config.id === userId) ||
  //     saturdayGroup1.value.some(config => config.id === userId) ||
  //     saturdayGroup2.value.some(config => config.id === userId) ||
  //     monthEndDutyList.value.some(config => config.id === userId)
}
// 1. 新增一个变量专门存储“部门+人员”的完整结构
const deptWithUsersList = ref([]);

// 2. 级联选项直接读取我们拼接好的 deptWithUsersList
const cascaderOptions = computed(() => {
  if (!deptWithUsersList.value || deptWithUsersList.value.length === 0) return [];

  // 1. 将常规部门装进 options 数组
  const options = deptWithUsersList.value.map(group => {
    return {
      value: `dept_${group.dept.id}`,
      label: group.dept.deptName,
      // 只有该部门下有人才渲染 children，否则不让展开
      children: group.users.length > 0 ? group.users.map(user => ({
        value: user.id,
        label: user.userName,
        account: user.userAccount,
        disabled: isUserAlreadyConfigured(user.id)
      })) : undefined
    };
  });

  // 2. 兜底逻辑：找出那些没有分配部门，或者部门被删除了的员工
  // 先提取出已经分配了部门的所有员工 ID
  const assignedUserIds = [];
  deptWithUsersList.value.forEach(group => {
    group.users.forEach(u => assignedUserIds.push(u.id));
  });

  // 找出全量人员里不在 assignedUserIds 中的人
  const unassignedUsers = allUsers.value.filter(user => !assignedUserIds.includes(user.id));

  // 如果有未分配部门的人员，单独建一个“未设置部门”的分组
  if (unassignedUsers.length > 0) {
    options.push({
      value: 'dept_unassigned',
      label: '未设置部门',
      children: unassignedUsers.map(user => ({
        value: user.id,
        label: user.userName,
        account: user.userAccount,
        disabled: isUserAlreadyConfigured(user.id)
      }))
    });
  }

  // 统一返回完整的选项列表
  return options;
});

const cascaderProps = {
  multiple: true,
  emitPath: false,
  value: 'value',
  label: 'label'
}

const calendarDays = computed(() => {
  const year = parseInt(currentDate.value.split('-')[0])
  const month = parseInt(currentDate.value.split('-')[1]) - 1
  const firstDay = new Date(year, month, 1)
  const lastDay = new Date(year, month + 1, 0)
  const firstDayWeek = firstDay.getDay()
  const daysInMonth = lastDay.getDate()

  const days = []
  for (let i = firstDayWeek - 1; i >= 0; i--) days.push(createDayData(new Date(year, month, -i), true))
  for (let day = 1; day <= daysInMonth; day++) days.push(createDayData(new Date(year, month, day), false))
  const remainingDays = 42 - days.length
  for (let day = 1; day <= remainingDays; day++) days.push(createDayData(new Date(year, month + 1, day), true))
  return days
})

const isSingleWeek = (date) => {
  if (!baseDate.value) return true
  const getSaturday = (d) => {
    const temp = new Date(d); temp.setHours(0, 0, 0, 0);
    temp.setDate(temp.getDate() + (6 - temp.getDay()));
    return temp
  }
  const baseSat = getSaturday(baseDate.value), targetSat = getSaturday(date)
  if (baseSat.getTime() === targetSat.getTime()) return true
  let isSingle = true, currentSat = new Date(baseSat)
  if (baseSat < targetSat) {
    while (currentSat < targetSat) {
      if (!isPublicHoliday(currentSat) && !isAdjustedWorkday(currentSat)) isSingle = !isSingle
      currentSat.setDate(currentSat.getDate() + 7)
    }
  } else {
    while (currentSat > targetSat) {
      currentSat.setDate(currentSat.getDate() - 7)
      if (!isPublicHoliday(currentSat) && !isAdjustedWorkday(currentSat)) isSingle = !isSingle
    }
  }
  return isSingle
}

const isMonthEndDay = (date) => {
  const day = date.getDate(), lastDay = new Date(date.getFullYear(), date.getMonth() + 1, 0).getDate()
  return day === lastDay || day === lastDay - 1
}

const createDayData = (date, isOtherMonth) => {
  const dayOfWeek = date.getDay(), isWeekendDay = isRestDay(date), isHoliday = isPublicHoliday(date), isAdjusted = isAdjustedWorkday(date)
  const today = new Date(); today.setHours(0, 0, 0, 0)
  const isToday = date.getTime() === today.getTime(), isPastDate = date < today
  const dateStr = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
  const recordsForDate = dutyRecordsCache.value ? dutyRecordsCache.value[dateStr] : null

  let dutyPersons = [], weekType = null
  if (dayOfWeek === 6) weekType = isSingleWeek(date) ? '单周' : '双周'

  if (isPastDate) {
    dutyPersons = recordsForDate?.map(r => ({ id: r.userId, userName: r.userName, dept: r.dept || '', phone: r.phone || '' })) || []
  } else {
    if (recordsForDate && recordsForDate.length > 0) {
      dutyPersons = recordsForDate.map(r => ({ id: r.userId, userName: r.userName, dept: r.dept || '', phone: r.phone || '' }))
    } else {
      let monthEndPersons = isMonthEndDay(date) ? monthEndDutyList.value.map(c => ({ id: c.id, userName: c.userName, dept: c.dept || '', phone: c.phone || '' })) : []
      let basePersons = []
      if ((dayOfWeek >= 1 && dayOfWeek <= 5 && !isHoliday) || isAdjusted) basePersons = weekdayDutyList.value.map(c => ({ id: c.id, userName: c.userName, dept: c.dept || '', phone: c.phone || '' }))
      else if ((dayOfWeek === 0 || dayOfWeek === 6) && !isHoliday && !isAdjusted && dayOfWeek === 6) {
        basePersons = (isSingleWeek(date) ? saturdayGroup1.value : saturdayGroup2.value).map(c => ({ id: c.id, userName: c.userName, dept: c.dept || '', phone: c.phone || '' }))
      }
      dutyPersons = [...basePersons, ...monthEndPersons]
    }
  }
  if (isHoliday) dutyPersons = []

  return { date, isOtherMonth, isWeekend: isWeekendDay, isHoliday, isAdjusted, weekType, isToday, isPastDate, dutyPersons }
}

const getWeekdayName = (date) => weekdays[date.getDay()]

const prevMonth = async () => {
  const date = new Date(currentDate.value + '-01')
  date.setMonth(date.getMonth() - 1)
  currentDate.value = date.toISOString().slice(0, 7)
  await refreshCalendar()
}
const nextMonth = async () => {
  const date = new Date(currentDate.value + '-01')
  date.setMonth(date.getMonth() + 1)
  currentDate.value = date.toISOString().slice(0, 7)
  await refreshCalendar()
}
const handleMonthChange = async () => await refreshCalendar()
const refreshCalendar = async (showMessage = true) => {
  await loadDutyRecords()
  if (showMessage) ElMessage.success('日历已刷新')
}

const loadDutyRecords = async () => {
  try {
    const year = parseInt(currentDate.value.split('-')[0]), month = parseInt(currentDate.value.split('-')[1]) - 1
    const startDateStr = `${year}-${String(month + 1).padStart(2, '0')}-01`
    const lastDay = new Date(year, month + 1, 0)
    const endDateStr = `${year}-${String(month + 1).padStart(2, '0')}-${String(lastDay.getDate()).padStart(2, '0')}`

    const res = await getDutyRecordsByDateRange(startDateStr, endDateStr)
    if (res.code === 0 && res.data) {
      const recordsMap = {}
      res.data.forEach(record => {
        const dStr = `${new Date(record.dutyDate).getFullYear()}-${String(new Date(record.dutyDate).getMonth() + 1).padStart(2, '0')}-${String(new Date(record.dutyDate).getDate()).padStart(2, '0')}`
        if (!recordsMap[dStr]) recordsMap[dStr] = []
        recordsMap[dStr].push(record)
      })
      dutyRecordsCache.value = recordsMap
    }
  } catch (error) { dutyRecordsCache.value = {} }
}

const handleDayClick = (day) => {
  if (day.isOtherMonth) return
  const dateStr = `${day.date.getFullYear()}-${String(day.date.getMonth() + 1).padStart(2, '0')}-${String(day.date.getDate()).padStart(2, '0')}`
  const isSat = day.date.getDay() === 6, isEnd = day.date.getDate() >= new Date(day.date.getFullYear(), day.date.getMonth() + 1, 0).getDate() - 1
  Object.assign(selectedDayInfo, { dateStr, weekdayName: getWeekdayName(day.date), isWeekend: day.isWeekend, dutyPersons: day.dutyPersons || [], isEditable: isSat || isEnd, isSaturday: isSat, isMonthEnd: isEnd, date: day.date })
  showDayDetailDialog.value = true
}

const dateEditMode = ref(false), editStartDate = ref(null), editDutyType = ref(null)

const handleEditDayDuty = () => {
  showDayDetailDialog.value = false; dateEditMode.value = true; editStartDate.value = selectedDayInfo.dateStr
  if (selectedDayInfo.isSaturday) {
    editDutyType.value = isSingleWeek(selectedDayInfo.date) ? 'saturday_group1' : 'saturday_group2'
    personForm.userIds = selectedDayInfo.dutyPersons.map(p => p.id)
  } else if (selectedDayInfo.isMonthEnd) {
    editDutyType.value = 'monthEnd'; personForm.userIds = selectedDayInfo.dutyPersons.map(p => p.id)
  }
  addPersonDialogTitle.value = selectedDayInfo.isSaturday ? `编辑${selectedDayInfo.dateStr}（周六）排班` : `编辑${selectedDayInfo.dateStr}（月末）排班`
  showAddPersonDialog.value = true
}

// 2. 点击添加/编辑按钮时，把现有人名单的 ID 传给表单，瞬间回显！
const handleAddWeekdayDuty = () => {
  currentSaturdayGroup.value = null;
  addPersonDialogTitle.value = '编辑工作日排班人员';
  resetPersonForm();
  personForm.userIds = weekdayDutyList.value.map(p => p.id); // <-- 加入这一行
  showAddPersonDialog.value = true;
}

const handleAddSaturdayDuty = (group) => {
  currentSaturdayGroup.value = group;
  addPersonDialogTitle.value = `编辑周六${group === 'group1' ? '单周' : '双周'}排班`;
  resetPersonForm();
  // <-- 加入这一行
  personForm.userIds = group === 'group1' ? saturdayGroup1.value.map(p => p.id) : saturdayGroup2.value.map(p => p.id);
  showAddPersonDialog.value = true;
}

const handleAddMonthEndDuty = () => {
  currentSaturdayGroup.value = null;
  addPersonDialogTitle.value = '编辑月末排班人员';
  resetPersonForm();
  personForm.userIds = monthEndDutyList.value.map(p => p.id); // <-- 加入这一行
  showAddPersonDialog.value = true;
}
const promptRemove = (person, callback) => {
  ElMessageBox.prompt('请输入移除原因', '移除确认', {
    confirmButtonText: '确定', cancelButtonText: '取消', inputPlaceholder: '请输入原因...',
    inputValidator: (v) => (!v || v.trim().length < 2) ? '至少需要2个字符' : true
  }).then(async ({ value: remark }) => {
    if (person.dutyPersonId) {
      const res = await deleteDutyPerson({ id: person.dutyPersonId, remark })
      if (res.code === 0) { ElMessage.success('移除成功'); await loadConfigFromLocal(); refreshCalendar() }
      else ElMessage.error(res.message || '移除失败')
    }
  }).catch(() => {})
}
const handleRemoveWeekdayDuty = (index) => promptRemove(weekdayDutyList.value[index])
const handleRemoveSaturdayDuty = (group, index) => promptRemove(group === 'group1' ? saturdayGroup1.value[index] : saturdayGroup2.value[index])
const handleRemoveMonthEndDuty = (index) => promptRemove(monthEndDutyList.value[index])

// 3. 二合一智能加载方法：先查部门，再查旗下人员，最后组装
const loadDeptAndUsers = async () => {
  if (deptWithUsersList.value.length > 0) return; // 如果已经加载过就不再重复加载

  loadingDept.value = true;
  loadingUserList.value = true;
  try {
    // 步骤 A：调用你的 /api/duty/getDept 接口
    const deptRes = await getDept();
    if (deptRes.code === 0 && deptRes.data) {
      const depts = deptRes.data;
      const enrichedUsers = []; // 用来收集带部门名称的全量用户

      // 步骤 B：并发调用你的 /api/user/getUserBydeptId/{deptId} 接口
      const promises = depts.map(async (dept) => {
        const userRes = await getUserBydeptId(dept.id, { current: 1, pageSize: 1000 });
        let users = [];
        if (userRes.code === 0 && userRes.data && userRes.data.records) {
          users = userRes.data.records;

          // 关键操作：由于后端 UserVO 没有 dept 字段，我们在这里手动帮它补上！
          // 这样保存排班记录时，表格里就能正常显示他们的部门名称了
          users.forEach(u => {
            u.dept = dept.deptName;
            enrichedUsers.push(u);
          });
        }
        return { dept, users };
      });

      // 步骤 C：等待所有请求完成，赋值给树形结构和全量列表
      deptWithUsersList.value = await Promise.all(promises);
      allUsers.value = enrichedUsers;
    }
  } catch (error) {
    console.error('获取部门人员数据失败', error);
  } finally {
    loadingDept.value = false;
    loadingUserList.value = false;
  }
};
const getUserNameById = (userId) => allUsers.value.find(u => u.id === userId)?.userName || '未知'
const handleRemoveSelectedUser = (userId) => { if (Array.isArray(personForm.userIds)) personForm.userIds = personForm.userIds.filter(id => id !== userId) }

// 3. 强化提交功能：实现批量新增与批量移除
const handleSubmitPerson = async () => {
  personFormRef.value.validate(async (valid) => {
    if (!valid || personForm.userIds.length === 0) { if(!valid) return; ElMessage.warning('请选择人员'); return }
    submittingPerson.value = true
    try {
      if (dateEditMode.value) {
        // ... (日历编辑模式，这段保持你原有的逻辑不变) ...
        let updateConfig = null; const newPersons = selectedUserIds.value.map(id => { const u = allUsers.value.find(user => user.id === id); return u ? { id: u.id, userName: u.userName, dept: u.dept, phone: u.phone } : null }).filter(Boolean)
        if (editDutyType.value === 'saturday_group1') { saturdayGroup1.value = newPersons; updateConfig = { saturdayGroup1: newPersons } }
        else if (editDutyType.value === 'saturday_group2') { saturdayGroup2.value = newPersons; updateConfig = { saturdayGroup2: newPersons } }
        else if (editDutyType.value === 'monthEnd') { monthEndDutyList.value = newPersons; updateConfig = { monthEndDutyList: newPersons } }
        if (updateConfig) { await updateDutyConfigFrom(editStartDate.value, updateConfig, personForm.remark); ElMessage.success('更新成功') }
      } else {
        // ==== 这里是配置管理模式的强化逻辑 ====
        let dutyType = "";
        let existingList = [];
        const title = addPersonDialogTitle.value;

        // 根据标题精准判断当前操作的是哪个组
        if (title.includes('工作日')) { dutyType = "weekday"; existingList = weekdayDutyList.value; }
        else if (title.includes('单周')) { dutyType = "saturday_group1"; existingList = saturdayGroup1.value; }
        else if (title.includes('双周')) { dutyType = "saturday_group2"; existingList = saturdayGroup2.value; }
        else if (title.includes('月末')) { dutyType = "month_end"; existingList = monthEndDutyList.value; }

        const existingIds = existingList.map(p => p.id);

        // 算差集1：现在勾选了，但原来没有的 -> 要新增的
        const newIdsToAdd = selectedUserIds.value.filter(id => !existingIds.includes(id));

        // 算差集2：原来有，但现在没被勾选的 -> 要删除的
        const removedPersons = existingList.filter(p => !selectedUserIds.value.includes(p.id));

        let successCount = 0;
        let removeCount = 0;

        // 1. 批量调用添加接口
        for (const userId of newIdsToAdd) {
          try { const res = await addDutyPerson({ userId, dutyType, remark: personForm.remark }); if (res.code === 0) successCount++ } catch (e) {}
        }

        // 2. 批量调用移除接口 (使用 dutyPersonId 主键)
        for (const person of removedPersons) {
          if (person.dutyPersonId) {
            try { const res = await deleteDutyPerson({ id: person.dutyPersonId, remark: personForm.remark || '批量取消选中移除' }); if (res.code === 0) removeCount++ } catch (e) {}
          }
        }

        if (successCount > 0 || removeCount > 0) {
          ElMessage.success(`操作成功：新增 ${successCount} 人，移除 ${removeCount} 人`);
        } else if (newIdsToAdd.length === 0 && removedPersons.length === 0) {
          ElMessage.info('没有进行任何修改');
        }
      }
      // 重新拉取一次最新配置，同步刷新日历
      if (!dateEditMode.value) await loadConfigFromLocal()
      showAddPersonDialog.value = false; resetPersonForm(); dateEditMode.value = false; await refreshCalendar(false)
    } finally { submittingPerson.value = false }
  })
}

const resetPersonForm = () => { Object.assign(personForm, { userIds: [], remark: '' }); personFormRef.value?.clearValidate() }
const handleBaseDateChange = async (newDate) => { if (newDate) { await saveBaseDate({ baseDate: newDate }); ElMessage.success('基准日期已保存'); await refreshCalendar() } }
const saveConfigToLocal = async () => { if (baseDate.value) await saveBaseDate({ baseDate: baseDate.value }) }
const loadConfigFromLocal = async () => { try { const res = await getDutyConfig(); if (res.code === 0 && res.data) { const c = res.data; weekdayDutyList.value = c.weekdayDutyList || []; saturdayGroup1.value = c.saturdayGroup1 || []; saturdayGroup2.value = c.saturdayGroup2 || []; monthEndDutyList.value = c.monthEndDutyList || []; if (c.baseDate) baseDate.value = new Date(c.baseDate).toISOString().slice(0, 10) } } catch (e) {} }

const handleSaveConfig = async () => { try { await saveConfigToLocal(); ElMessage.success('配置已保存'); showConfigDialog.value = false; refreshCalendar() } catch (e) { ElMessage.error('保存失败') } }

const handleGenerateRecords = async () => {
  if (!generateForm.startDate || !generateForm.endDate) return ElMessage.warning('请选择起止日期')
  if (new Date(generateForm.startDate) > new Date(generateForm.endDate)) return ElMessage.warning('开始不能晚于结束日期')
  generatingRecords.value = true
  try {
    const res = await generateDutyRecords(generateForm)
    if (res.code === 0) { ElMessage.success(`成功生成 ${res.data} 条记录`); showGenerateDialog.value = false; generateForm.startDate = ''; generateForm.endDate = '' }
  } finally { generatingRecords.value = false }
}

const groupByDept = (persons) => { const groups = {}; persons.forEach(p => { const d = p.dept || '未设置'; if (!groups[d]) groups[d] = []; groups[d].push(p) }); return groups }
const formatDateHeader = (dateStr, isSaturday) => { if (!dateStr) return ''; const d = new Date(dateStr); return `${isSaturday ? '周六' : '周日'}\n（${d.getMonth() + 1}月${d.getDate()}日）` }

const exportDetailToPng = async () => {
  exportLoading.value = true
  try {
    await nextTick()
    const container = exportContainer.value
    if (!container) return
    const targetNode = container.querySelector('.duty-table-wrapper') || container
    const rect = targetNode.getBoundingClientRect()
    const canvas = await html2canvas(targetNode, {
      scale: 2, useCORS: true, logging: false, backgroundColor: '#ffffff',
      width: targetNode.scrollWidth, height: Math.max(rect.height, targetNode.scrollHeight) + 100,
      onclone: (doc) => { const el = doc.querySelector('.export-container'); if(el) { el.style.padding = '30px'; el.style.height = 'max-content' } }
    })
    const blob = await new Promise(r => canvas.toBlob(r, 'image/png'))
    const url = URL.createObjectURL(blob); const a = document.createElement('a'); a.download = `排班_${selectedDayInfo.dateStr}.png`; a.href = url; a.click(); URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } finally { exportLoading.value = false }
}

const initUserInfo = () => { try { const userInfo = localStorage.getItem('userInfo'); if (userInfo) loginUserStore.setLoginUser(JSON.parse(userInfo)) } catch (e) {} }

onMounted(async () => {
  await initHolidayConfig();
  initUserInfo();
  await loadConfigFromLocal();
  if (isAdmin.value) {
    // 替换为我们写好的新方法
    await loadDeptAndUsers();
  }
  refreshCalendar();
});

watch(showConfigDialog, (val) => {
  if (val && isAdmin.value) {
    loadDeptAndUsers();
  }
});

watch(showAddPersonDialog, (val) => {
  if (val && isAdmin.value) {
    loadDeptAndUsers();
  }
});
</script>

<style scoped>
/* =========================================
   Apple Theme 核心样式重塑
   ========================================= */
.apple-theme {
  --apple-bg: #f5f5f7;
  --apple-card: #ffffff;
  --apple-text-main: #1d1d1f;
  --apple-text-sub: #86868b;
  --apple-blue: #007aff;
  --apple-blue-hover: #006ce6;
  --apple-blue-light: #e5f0ff;
  --apple-red: #ff3b30;
  --apple-border: rgba(0, 0, 0, 0.06);
  --apple-shadow-sm: 0 2px 8px rgba(0, 0, 0, 0.04);
  --apple-shadow-md: 0 8px 24px rgba(0, 0, 0, 0.08);
  --apple-shadow-lg: 0 20px 40px rgba(0, 0, 0, 0.12);
  --apple-radius-sm: 8px;
  --apple-radius-md: 14px;
  --apple-radius-lg: 20px;

  /* 覆盖 Element Plus 的默认变量以全局契合风格 */
  --el-color-primary: var(--apple-blue);
  --el-border-radius-base: var(--apple-radius-sm);
  --el-text-color-primary: var(--apple-text-main);
  --el-text-color-regular: var(--apple-text-sub);
}

.home-page {
  font-family: -apple-system, BlinkMacSystemFont, "SF Pro Text", "PingFang SC", "Helvetica Neue", Arial, sans-serif;
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
}
.apple-btn:not(.primary):hover {
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
.apple-btn.primary-light {
  background-color: var(--apple-blue-light);
  color: var(--apple-blue);
  border: none;
}
.apple-btn.success {
  background-color: #34c759;
  border: none;
  color: white;
}
.apple-btn.success:hover {
  background-color: #2eb350;
}
.apple-icon-btn {
  border: none;
  background: var(--apple-bg);
  color: var(--apple-text-main);
}
.apple-icon-btn:hover {
  background: #e5e5ea;
}

/* --- 日历卡片主容器 --- */
.calendar-card {
  background: var(--apple-card);
  border-radius: var(--apple-radius-lg);
  box-shadow: var(--apple-shadow-md);
  padding: 24px;
  border: 1px solid rgba(0,0,0,0.02);
}

/* --- 控制栏 (月份切换等) --- */
.month-selector {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-bottom: 24px;
}

.current-month-wrapper {
  background: var(--apple-bg);
  border-radius: var(--apple-radius-sm);
  padding: 2px 4px;
}

:deep(.apple-date-picker .el-input__wrapper) {
  background: transparent;
  border: none !important;
  box-shadow: none !important;
}
:deep(.apple-date-picker .el-input__inner) {
  font-size: 17px;
  font-weight: 600;
  color: var(--apple-text-main);
  text-align: center;
}

.divider {
  width: 1px;
  height: 24px;
  background-color: var(--apple-border);
  margin: 0 12px;
}

/* --- 日历网格 --- */
.calendar-grid {
  border-radius: var(--apple-radius-md);
  border: 1px solid var(--apple-border);
  overflow: hidden;
  background: var(--apple-border); /* 用背景色做网格线 */
}

.calendar-weekdays {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  background: var(--apple-card);
  gap: 1px;
  border-bottom: 1px solid var(--apple-border);
}

.weekday-header {
  padding: 12px 0;
  text-align: center;
  font-size: 13px;
  font-weight: 600;
  color: var(--apple-text-sub);
  background: #fafafa;
}

.calendar-days {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 1px; /* 生成 1px 网格线 */
}

.calendar-day {
  background: var(--apple-card);
  min-height: 120px;
  padding: 10px;
  display: flex;
  flex-direction: column;
  transition: background 0.2s ease;
  cursor: pointer;
}

.calendar-day:hover {
  background: #f9f9fb;
}

.calendar-day.other-month {
  background: #fdfdfd;
  opacity: 0.6;
}

.calendar-day.weekend {
  background: #fefcfc; /* 极浅极浅的红/橙暗示周末 */
}

.day-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8px;
}

.day-number-wrapper {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 50%;
}

.day-number {
  font-size: 15px;
  font-weight: 500;
  color: var(--apple-text-main);
}

/* 今天的高亮 */
.calendar-day.today .day-number-wrapper {
  background-color: var(--apple-blue);
}
.calendar-day.today .day-number {
  color: #ffffff;
  font-weight: 700;
}

/* 节假目标签 */
.day-tags {
  display: flex;
  gap: 4px;
}
.apple-tag {
  font-size: 10px;
  font-weight: 600;
  padding: 2px 6px;
  border-radius: 4px;
  line-height: 1.2;
}
.holiday-tag { background: #ffebeb; color: var(--apple-red); }
.workday-tag { background: var(--apple-blue-light); color: var(--apple-blue); }
.week-type-tag { background: #f0f0f2; color: var(--apple-text-sub); }

/* --- 值班信息 --- */
.day-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.duty-pills {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 4px;
}

.duty-pill {
  display: inline-flex;
  align-items: center;
  background: var(--apple-blue-light);
  color: var(--apple-blue);
  border-radius: 6px;
  padding: 4px 8px;
  font-size: 12px;
  font-weight: 600;
}
.duty-pill.is-weekend-pill {
  background: #fff0eb;
  color: #ff9500; /* Apple Orange */
}

.pill-count {
  margin-left: 4px;
  opacity: 0.7;
}

.duty-details-mini {
  margin-top: auto;
}

.detail-text {
  font-size: 11px;
  color: var(--apple-text-sub);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.no-duty {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}
.no-duty-text {
  font-size: 12px;
  color: #d1d1d6;
  font-weight: 500;
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

/* --- 配置选项卡 --- */
:deep(.apple-tabs .el-tabs__item) {
  font-weight: 500;
  font-size: 15px;
}
:deep(.apple-tabs .el-tabs__item.is-active) {
  font-weight: 600;
}
:deep(.apple-tabs .el-tabs__nav-wrap::after) {
  height: 1px;
  background-color: var(--apple-border);
}

.config-section {
  padding-top: 12px;
}
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.section-desc {
  color: var(--apple-text-sub);
  font-size: 13px;
}

.apple-sub-card {
  background: #fafafa;
  border: 1px solid var(--apple-border);
  border-radius: var(--apple-radius-md);
  padding: 16px;
}
.group-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.group-header h4 {
  margin: 0;
  font-weight: 600;
  color: var(--apple-text-main);
}

/* Apple Table */
:deep(.apple-table) {
  --el-table-border-color: var(--apple-border);
  --el-table-header-bg-color: #f5f5f7;
  border-radius: var(--apple-radius-sm);
  overflow: hidden;
}
:deep(.apple-table th.el-table__cell) {
  font-weight: 600;
  color: var(--apple-text-main);
  background-color: #f5f5f7;
}

.apple-text-btn {
  font-weight: 500;
}
.apple-text-btn.danger {
  color: var(--apple-red);
}

/* --- Apple 风格表单 (Form) --- */
:deep(.apple-form .el-form-item__label) {
  font-weight: 600;
  color: var(--apple-text-main);
  padding-bottom: 6px;
}
:deep(.apple-textarea .el-textarea__inner),
:deep(.apple-cascader .el-input__wrapper) {
  border-radius: var(--apple-radius-sm);
  box-shadow: 0 0 0 1px var(--apple-border) inset;
  background: #fafafa;
  transition: all 0.2s;
}
:deep(.apple-textarea .el-textarea__inner:focus),
:deep(.apple-cascader .el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px var(--apple-blue) inset;
  background: #ffffff;
}

.apple-selected-users {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 12px;
  background: #f5f5f7;
  border-radius: var(--apple-radius-sm);
}
.apple-user-tag {
  background: #ffffff;
  border: 1px solid var(--apple-border);
  border-radius: 16px;
  padding: 4px 10px;
  font-size: 13px;
  font-weight: 500;
  color: var(--apple-text-main);
  display: inline-flex;
  align-items: center;
  box-shadow: 0 1px 2px rgba(0,0,0,0.02);
}
.tag-close {
  margin-left: 6px;
  color: var(--apple-text-sub);
  cursor: pointer;
  font-size: 14px;
}
.tag-close:hover {
  color: var(--apple-red);
}

/* --- 提示框 (Alert) --- */
.apple-alert {
  display: flex;
  align-items: flex-start;
  background: var(--apple-blue-light);
  border-radius: var(--apple-radius-sm);
  padding: 16px;
  color: var(--apple-blue);
}
.apple-alert .el-icon {
  font-size: 20px;
  margin-right: 12px;
  margin-top: 2px;
}
.alert-content p {
  margin: 0 0 4px 0;
  font-size: 13px;
  line-height: 1.5;
}
.alert-content p:last-child {
  margin-bottom: 0;
}

/* --- 导出/详情对话框 --- */
.detail-actions {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}
.apple-export-wrapper {
  background: #ffffff;
  border-radius: var(--apple-radius-md);
  padding: 32px;
  border: 1px solid var(--apple-border);
}

.apple-export-title {
  text-align: center;
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 24px;
  color: #000;
  letter-spacing: 1px;
}

.duty-export-table {
  width: 100%;
  border-collapse: collapse;
}
.duty-export-table th,
.duty-export-table td {
  border: 1px solid #d1d1d6;
  padding: 16px;
  text-align: center;
  color: #1d1d1f;
}
.duty-export-table th {
  background-color: #f5f5f7;
  font-weight: 600;
  font-size: 15px;
}
.dept-cell {
  font-weight: 600;
  background-color: #fafafa;
}
.person-info-cell {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}
.person-name-text {
  font-weight: 600;
  font-size: 15px;
}
.person-phone-text {
  color: #86868b;
  font-size: 14px;
}
.empty-message {
  padding: 32px !important;
  color: #86868b;
  font-weight: 500;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .home-page { padding: 16px; }
  .calendar-card { padding: 16px; }
  .page-header { flex-direction: column; align-items: flex-start; gap: 16px; }
  .month-selector { flex-wrap: wrap; }
  .calendar-day { min-height: 80px; padding: 6px; }
  .day-number { font-size: 13px; }
  .duty-pill { padding: 2px 4px; font-size: 10px; }
}
</style>