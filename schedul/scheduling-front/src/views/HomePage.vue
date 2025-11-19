<template>
  <div class="home-page">
    <div class="page-header">
      <h1 class="page-title">值班安排表</h1>
      <div class="header-actions" v-if="isAdmin">
        <el-button :icon="Setting" @click="showConfigDialog = true">配置管理</el-button>
        <el-button type="warning" :icon="DocumentAdd" @click="showGenerateDialog = true">生成值班记录</el-button>
      </div>
    </div>

    <div class="calendar-container">
      <!-- 月份切换 -->
      <div class="month-selector">
        <el-button :icon="ArrowLeft" circle @click="prevMonth" />
        <div class="current-month">
          <el-date-picker
            v-model="currentDate"
            type="month"
            placeholder="选择月份"
            format="YYYY年MM月"
            value-format="YYYY-MM"
            @change="handleMonthChange"
            style="width: 180px;"
          />
        </div>
        <el-button :icon="ArrowRight" circle @click="nextMonth" />
        <el-button :icon="Refresh" @click="refreshCalendar" style="margin-left: 16px;">刷新</el-button>
      </div>

      <!-- 日历网格 -->
      <div class="calendar-grid">
        <!-- 星期标题 -->
        <div class="calendar-weekdays">
          <div v-for="day in weekdays" :key="day" class="weekday-header">
            {{ day }}
          </div>
        </div>

        <!-- 日期单元格 -->
        <div class="calendar-days">
          <div
            v-for="(day, index) in calendarDays"
            :key="index"
            class="calendar-day"
            :class="{
              'other-month': day.isOtherMonth,
              'today': day.isToday,
              'weekend': day.isWeekend
            }"
          >
            <div class="day-header">
              <span class="day-number">{{ day.date.getDate() }}</span>
              <span class="day-weekday">{{ getWeekdayName(day.date) }}</span>
            </div>
            <div class="day-content" @click="handleDayClick(day)">
              <div v-if="day.dutyPersons && day.dutyPersons.length > 0" class="duty-info">
                <div class="duty-person">
                  <el-tag :type="day.isWeekend ? 'warning' : 'primary'" size="small">
                    {{ day.dutyPersons[0].userName }}
                  </el-tag>
                  <span v-if="day.dutyPersons.length > 1" class="more-count">+{{ day.dutyPersons.length - 1 }}</span>
                </div>
                <div class="duty-details">
                  <div class="detail-item">
                    <span class="label">部门：</span>
                    <span class="value">{{ day.dutyPersons[0].dept || '未设置' }}</span>
                  </div>
                  <div class="detail-item">
                    <span class="label">手机：</span>
                    <span class="value">{{ day.dutyPersons[0].phone || '未设置' }}</span>
                  </div>
                </div>
              </div>
              <div v-else class="no-duty">
                <span class="no-duty-text">未安排</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 配置管理对话框 -->
    <el-dialog
      v-model="showConfigDialog"
      title="值班配置管理"
      width="1000px"
      :close-on-click-modal="false"
    >
      <el-tabs v-model="configTab" type="border-card">
        <!-- 工作日值班配置（周一到周五） -->
        <el-tab-pane label="工作日值班（周一到周五）" name="weekday">
          <div class="config-section">
            <div class="section-header">
              <span class="section-desc">工作日值班人员固定，每周都值班</span>
              <el-button type="primary" :icon="Plus" @click="handleAddWeekdayDuty">添加值班人员</el-button>
            </div>
            <el-table :data="weekdayDutyList" border style="width: 100%; margin-top: 16px;" v-loading="loadingUsers">
              <el-table-column prop="userName" label="姓名" width="120" />
              <el-table-column prop="dept" label="部门" width="150" />
              <el-table-column prop="phone" label="手机号" width="150" />
              <el-table-column label="操作" width="150" align="center">
                <template #default="{ row, $index }">
                  <el-button type="danger" link :icon="Delete" @click="handleRemoveWeekdayDuty($index)">
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

        <!-- 周六值班配置（单周/双周轮班） -->
        <el-tab-pane label="周六值班（单周/双周轮班）" name="saturday">
          <div class="config-section">
            <div class="section-header">
              <div class="base-date-config">
                <span>基准日期：</span>
                <el-date-picker
                  v-model="baseDate"
                  type="date"
                  placeholder="选择基准日期"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                  style="width: 200px; margin-right: 16px;"
                  @change="handleBaseDateChange"
                />
                <span class="date-desc">（用于判断单周/双周，该日期所在周为单周）</span>
              </div>
            </div>
            
            <el-row :gutter="20" style="margin-top: 20px;">
              <!-- 单周组 -->
              <el-col :span="12">
                <div class="saturday-group">
                  <div class="group-header">
                    <h4>单周组</h4>
                    <el-button type="primary" size="small" :icon="Plus" @click="handleAddSaturdayDuty('group1')">
                      添加人员
                    </el-button>
                  </div>
                  <el-table :data="saturdayGroup1" border style="width: 100%; margin-top: 16px;" v-loading="loadingUsers">
                    <el-table-column prop="userName" label="姓名" width="100" />
                    <el-table-column prop="dept" label="部门" width="100" />
                    <el-table-column prop="phone" label="手机号" width="120" />
                    <el-table-column label="操作" width="80" align="center">
                      <template #default="{ row, $index }">
                        <el-button type="danger" link size="small" :icon="Delete" @click="handleRemoveSaturdayDuty('group1', $index)">
                          删除
                        </el-button>
                      </template>
                    </el-table-column>
                  </el-table>
                </div>
              </el-col>

              <!-- 双周组 -->
              <el-col :span="12">
                <div class="saturday-group">
                  <div class="group-header">
                    <h4>双周组</h4>
                    <el-button type="primary" size="small" :icon="Plus" @click="handleAddSaturdayDuty('group2')">
                      添加人员
                    </el-button>
                  </div>
                  <el-table :data="saturdayGroup2" border style="width: 100%; margin-top: 16px;" v-loading="loadingUsers">
                    <el-table-column prop="userName" label="姓名" width="100" />
                    <el-table-column prop="dept" label="部门" width="100" />
                    <el-table-column prop="phone" label="手机号" width="120" />
                    <el-table-column label="操作" width="80" align="center">
                      <template #default="{ row, $index }">
                        <el-button type="danger" link size="small" :icon="Delete" @click="handleRemoveSaturdayDuty('group2', $index)">
                          删除
                        </el-button>
                      </template>
                    </el-table-column>
                  </el-table>
                </div>
              </el-col>
            </el-row>
          </div>
        </el-tab-pane>

        <!-- 月末值班配置（最后两天） -->
        <el-tab-pane label="月末值班（最后两天）" name="monthEnd">
          <div class="config-section">
            <div class="section-header">
              <span class="section-desc">每月最后两天安排部分人员值班（加班发工资）</span>
              <el-button type="primary" :icon="Plus" @click="handleAddMonthEndDuty">添加值班人员</el-button>
            </div>
            <el-table :data="monthEndDutyList" border style="width: 100%; margin-top: 16px;" v-loading="loadingUsers">
              <el-table-column prop="userName" label="姓名" width="120" />
              <el-table-column prop="dept" label="部门" width="150" />
              <el-table-column prop="phone" label="手机号" width="150" />
              <el-table-column label="操作" width="150" align="center">
                <template #default="{ row, $index }">
                  <el-button type="danger" link :icon="Delete" @click="handleRemoveMonthEndDuty($index)">
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
      </el-tabs>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showConfigDialog = false">取消</el-button>
          <el-button type="primary" @click="handleSaveConfig">保存配置</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 添加值班人员对话框 - 选择用户 -->
    <el-dialog
      v-model="showAddPersonDialog"
      :title="addPersonDialogTitle"
      width="600px"
      @close="resetPersonForm"
    >
      <el-form
        ref="personFormRef"
        :model="personForm"
        :rules="personFormRules"
        label-width="100px"
      >
        <el-form-item label="选择用户" prop="userIds">
          <el-select
            v-model="personForm.userIds"
            placeholder="请选择用户（可多选）"
            multiple
            filterable
            style="width: 100%;"
            :loading="loadingUserList"
            collapse-tags
            collapse-tags-tooltip
          >
            <el-option
              v-for="user in availableUsers"
              :key="user.id"
              :label="`${user.userName} (${user.userAccount})`"
              :value="user.id"
            >
              <div style="display: flex; justify-content: space-between; align-items: center;">
                <span>{{ user.userName }}</span>
                <div style="display: flex; align-items: center; gap: 8px;">
                  <span style="color: #909399; font-size: 12px;">{{ user.userAccount }}</span>
                  <el-tag 
                    v-if="isUserAlreadyConfigured(user.id)" 
                    size="small" 
                    type="info"
                    style="font-size: 10px;"
                  >
                    已配置
                  </el-tag>
                </div>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="已选人员" v-if="personForm.userIds && personForm.userIds.length > 0">
          <div class="selected-users-list">
            <el-tag
              v-for="userId in personForm.userIds"
              :key="userId"
              closable
              @close="handleRemoveSelectedUser(userId)"
              style="margin-right: 8px; margin-bottom: 8px;"
            >
              {{ getUserNameById(userId) }}
            </el-tag>
          </div>
        </el-form-item>
        <el-form-item label="调班备注" prop="remark">
          <el-input
            v-model="personForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入调班原因或备注信息"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showAddPersonDialog = false">取消</el-button>
          <el-button type="primary" @click="handleSubmitPerson" :loading="submittingPerson">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 生成值班记录对话框 -->
    <el-dialog
      v-model="showGenerateDialog"
      title="生成值班记录"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="generateForm" label-width="100px">
        <el-form-item label="开始日期">
          <el-date-picker
            v-model="generateForm.startDate"
            type="date"
            placeholder="选择开始日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
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
            style="width: 100%"
          />
        </el-form-item>
        <el-alert
          title="说明"
          type="info"
          :closable="false"
          style="margin-bottom: 16px;"
        >
          <p>此功能用于生成指定日期范围内的值班记录。</p>
          <p>系统会每天凌晘02:00自动生成当天的值班记录。</p>
          <p>如果需要补生成历史记录，请手动使用此功能。</p>
        </el-alert>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showGenerateDialog = false">取消</el-button>
          <el-button type="primary" @click="handleGenerateRecords" :loading="generatingRecords">生成</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 查看日期详情对话框（含新导出样式） -->
    <el-dialog
      v-model="showDayDetailDialog"
      title="值班人员详情"
      width="600px"
    >
      <!-- 导出按钮和编辑按钮 -->
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
        <!-- 左侧编辑按钮（仅管理员且可编辑日期可见） -->
        <el-button
          v-if="isAdmin && selectedDayInfo.isEditable"
          type="primary"
          :icon="Edit"
          @click="handleEditDayDuty"
        >
          编辑排班
        </el-button>
        <div v-else></div>
        
        <!-- 右侧导出按钮 -->
        <el-button 
          type="success" 
          :icon="Download" 
          @click="exportDetailToPng"
          :loading="exportLoading"
        >
          导出详情为PNG
        </el-button>
      </div>
      
      <!-- 导出目标容器（添加ref标识，用于截图） -->
      <div ref="exportContainer" class="export-container">
         <div class="duty-table-wrapper">
          <h2 class="table-title">锦途周末值班表</h2>
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
                        <span class="person-name-text">{{ person.userName }}：</span>
                        <span class="person-phone-text">{{ person.phone || '未设置' }}</span>
                      </div>
                    </td>
                  </tr>
                </template>
              </template>
              <tr v-else>
                <td colspan="2" class="empty-message">该日期未安排值班人员</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>



      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showDayDetailDialog = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
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
  updateDutyConfigFrom
} from '@/api/index'
import { useLoginUserStore } from '@/stores/useLoginUserStore'

// Pinia Store
const loginUserStore = useLoginUserStore()

// 导出相关状态
const exportContainer = ref(null)
const exportLoading = ref(false)

// 响应式数据
const currentDate = ref(new Date().toISOString().slice(0, 7))
const showConfigDialog = ref(false)
const showGenerateDialog = ref(false)
const generatingRecords = ref(false)
const generateForm = reactive({
  startDate: '',
  endDate: ''
})

// 管理员判断
const isAdmin = computed(() => {
  if (loginUserStore.loginUser && loginUserStore.loginUser.userRole === 'admin') {
    return true
  }
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

const showAddPersonDialog = ref(false)
const showDayDetailDialog = ref(false)
const addPersonDialogTitle = ref('添加值班人员')
const personFormRef = ref(null)
const editIndex = ref(-1)
const isEditMode = computed(() => editIndex.value >= 0)
const loadingUsers = ref(false)
const loadingUserList = ref(false)
const submittingPerson = ref(false)

// 用户列表
const allUsers = ref([])
const availableUsers = computed(() => allUsers.value)

// 检查用户是否已配置
const isUserAlreadyConfigured = (userId) => {
  return weekdayDutyList.value.some(config => config.id === userId) ||
         saturdayGroup1.value.some(config => config.id === userId) ||
         saturdayGroup2.value.some(config => config.id === userId) ||
         monthEndDutyList.value.some(config => config.id === userId)
}

// 星期标题
const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']

// 配置标签页
const configTab = ref('weekday')

// 值班人员列表
const weekdayDutyList = ref([])
const saturdayGroup1 = ref([])
const saturdayGroup2 = ref([])
const monthEndDutyList = ref([])

// 基准日期
const baseDate = ref(new Date().toISOString().slice(0, 10))
const currentSaturdayGroup = ref('group1')

// 值班记录缓存
const dutyRecordsCache = ref({})

// 选中日期信息
const selectedDayInfo = reactive({
  dateStr: '',
  weekdayName: '',
  isWeekend: false,
  dutyPersons: [],
  isEditable: false,
  isSaturday: false,
  isMonthEnd: false,
  date: null
})

// 人员表单数据
const personForm = reactive({
  userIds: [],
  remark: ''
})

// 表单验证规则
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

// 计算当前月份的所有日期
const calendarDays = computed(() => {
  const year = parseInt(currentDate.value.split('-')[0])
  const month = parseInt(currentDate.value.split('-')[1]) - 1
  const firstDay = new Date(year, month, 1)
  const lastDay = new Date(year, month + 1, 0)
  const firstDayWeek = firstDay.getDay()
  const daysInMonth = lastDay.getDate()
  
  const days = []
  const today = new Date()
  today.setHours(0, 0, 0, 0)

  // 添加上个月的最后几天
  for (let i = firstDayWeek - 1; i >= 0; i--) {
    const date = new Date(year, month, -i)
    days.push(createDayData(date, true))
  }

  // 添加当月的所有日期
  for (let day = 1; day <= daysInMonth; day++) {
    const date = new Date(year, month, day)
    days.push(createDayData(date, false))
  }

  // 添加下个月的前几天
  const remainingDays = 42 - days.length
  for (let day = 1; day <= remainingDays; day++) {
    const date = new Date(year, month + 1, day)
    days.push(createDayData(date, true))
  }

  return days
})

// 判断单周/双周
const isSingleWeek = (date) => {
  if (!baseDate.value) return true
  const base = new Date(baseDate.value)
  base.setHours(0, 0, 0, 0)
  const target = new Date(date)
  target.setHours(0, 0, 0, 0)
  const diffTime = target.getTime() - base.getTime()
  const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24))
  const diffWeeks = Math.floor(diffDays / 7)
  return diffWeeks % 2 === 0
}

// 判断是否为月末最后两天
const isMonthEndDay = (date) => {
  const year = date.getFullYear()
  const month = date.getMonth()
  const day = date.getDate()
  const lastDay = new Date(year, month + 1, 0).getDate()
  return day === lastDay || day === lastDay - 1
}

// 创建日期数据对象
const createDayData = (date, isOtherMonth) => {
  const dayOfWeek = date.getDay()
  const isWeekend = dayOfWeek === 0 || dayOfWeek === 6
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const isToday = date.getTime() === today.getTime()
  const isPastDate = date < today

  const formatDate = (date) => {
    const y = date.getFullYear()
    const m = String(date.getMonth() + 1).padStart(2, '0')
    const d = String(date.getDate()).padStart(2, '0')
    return `${y}-${m}-${d}`
  }
  const dateStr = formatDate(date)
  const recordsForDate = dutyRecordsCache.value ? dutyRecordsCache.value[dateStr] : null
  
  let dutyPersons = []
  if (isPastDate) {
    dutyPersons = recordsForDate?.map(record => ({
      id: record.userId,
      userName: record.userName,
      dept: record.dept || '',
      phone: record.phone || ''
    })) || []
  } else {
    if (recordsForDate && recordsForDate.length > 0) {
      dutyPersons = recordsForDate.map(record => ({
        id: record.userId,
        userName: record.userName,
        dept: record.dept || '',
        phone: record.phone || ''
      }))
    } else {
      const isMonthEnd = isMonthEndDay(date)
      if (isMonthEnd) {
        dutyPersons = monthEndDutyList.value.map(config => ({
          id: config.id,
          userName: config.userName,
          dept: config.dept || '',
          phone: config.phone || ''
        }))
      }
      if (dayOfWeek >= 1 && dayOfWeek <= 5) {
        const weekdayPersons = weekdayDutyList.value.map(config => ({
          id: config.id,
          userName: config.userName,
          dept: config.dept || '',
          phone: config.phone || ''
        }))
        dutyPersons = [...dutyPersons, ...weekdayPersons]
      } else if (dayOfWeek === 6) {
        const isSingle = isSingleWeek(date)
        const saturdayGroup = isSingle ? saturdayGroup1.value : saturdayGroup2.value
        const saturdayPersons = saturdayGroup.map(config => ({
          id: config.id,
          userName: config.userName,
          dept: config.dept || '',
          phone: config.phone || ''
        }))
        dutyPersons = [...dutyPersons, ...saturdayPersons]
      }
    }
  }

  return {
    date,
    isOtherMonth,
    isWeekend,
    isToday,
    isPastDate,
    dutyPersons
  }
}

// 获取星期名称
const getWeekdayName = (date) => weekdays[date.getDay()]

// 月份切换
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

const handleMonthChange = async () => {
  await refreshCalendar()
}

// 刷新日历
const refreshCalendar = async (showMessage = true) => {
  await loadDutyRecords()
  if (showMessage) {
    ElMessage.success('日历已刷新')
  }
}

// 加载值班记录
const loadDutyRecords = async () => {
  try {
    const year = parseInt(currentDate.value.split('-')[0])
    const month = parseInt(currentDate.value.split('-')[1]) - 1
    const firstDay = new Date(year, month, 1)
    const lastDay = new Date(year, month + 1, 0)
    
    const formatDate = (date) => {
      const y = date.getFullYear()
      const m = String(date.getMonth() + 1).padStart(2, '0')
      const d = String(date.getDate()).padStart(2, '0')
      return `${y}-${m}-${d}`
    }
    const startDateStr = formatDate(firstDay)
    const endDateStr = formatDate(lastDay)
    
    const res = await getDutyRecordsByDateRange(startDateStr, endDateStr)
    if (res.code === 0 && res.data) {
      const recordsMap = {}
      res.data.forEach(record => {
        const date = new Date(record.dutyDate)
        const dateStr = formatDate(date)
        if (!recordsMap[dateStr]) {
          recordsMap[dateStr] = []
        }
        recordsMap[dateStr].push(record)
      })
      dutyRecordsCache.value = recordsMap
      console.log('加载历史值班记录:', Object.keys(recordsMap).length, '天')
    }
  } catch (error) {
    console.error('加载值班记录失败:', error)
    dutyRecordsCache.value = {}
  }
}

// 点击日期
const handleDayClick = (day) => {
  if (day.isOtherMonth) return
  const year = day.date.getFullYear()
  const month = String(day.date.getMonth() + 1).padStart(2, '0')
  const date = String(day.date.getDate()).padStart(2, '0')
  const dateStr = `${year}-${month}-${date}`
  const isSaturday = day.date.getDay() === 6
  const monthLastDay = new Date(year, month, 0).getDate()
  const isMonthEnd = date >= monthLastDay - 1
  
  Object.assign(selectedDayInfo, {
    dateStr: dateStr,
    weekdayName: getWeekdayName(day.date),
    isWeekend: day.isWeekend,
    dutyPersons: day.dutyPersons || [],
    isEditable: isSaturday || isMonthEnd,
    isSaturday: isSaturday,
    isMonthEnd: isMonthEnd,
    date: day.date
  })
  
  showDayDetailDialog.value = true
}

// 编辑当天排班
const handleEditDayDuty = () => {
  showDayDetailDialog.value = false
  dateEditMode.value = true
  editStartDate.value = selectedDayInfo.dateStr
  
  if (selectedDayInfo.isSaturday) {
    const isSingle = isSingleWeek(selectedDayInfo.date)
    editDutyType.value = isSingle ? 'saturday_group1' : 'saturday_group2'
    personForm.userIds = selectedDayInfo.dutyPersons.map(person => person.id)
  } else if (selectedDayInfo.isMonthEnd) {
    editDutyType.value = 'monthEnd'
    personForm.userIds = selectedDayInfo.dutyPersons.map(person => person.id)
  }
  
  addPersonDialogTitle.value = selectedDayInfo.isSaturday 
    ? `编辑${selectedDayInfo.dateStr}（周六）值班人员` 
    : `编辑${selectedDayInfo.dateStr}（月末）值班人员`
  showAddPersonDialog.value = true
}

// 编辑模式标识
const dateEditMode = ref(false)
const editStartDate = ref(null)
const editDutyType = ref(null)

// 添加/编辑值班人员
const handleAddWeekdayDuty = () => {
  currentSaturdayGroup.value = null
  addPersonDialogTitle.value = '添加工作日值班人员'
  resetPersonForm()
  showAddPersonDialog.value = true
}

const handleAddSaturdayDuty = (group) => {
  currentSaturdayGroup.value = group
  addPersonDialogTitle.value = `添加周六${group === 'group1' ? '单周' : '双周'}组值班人员`
  resetPersonForm()
  showAddPersonDialog.value = true
}

const handleAddMonthEndDuty = () => {
  currentSaturdayGroup.value = null
  addPersonDialogTitle.value = '添加月末值班人员'
  resetPersonForm()
  showAddPersonDialog.value = true
}

// 删除值班人员
const handleRemoveWeekdayDuty = async (index) => {
  const person = weekdayDutyList.value[index]
  ElMessageBox.prompt('请输入删除原因', '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPlaceholder: '请输入调班原因',
    inputValidator: (value) => {
      if (!value || value.trim().length < 2) return '备注至少需要2个字符'
      if (value.length > 200) return '备注长度不能超过200个字符'
      return true
    },
  }).then(async ({ value: remark }) => {
    if (person.dutyPersonId) {
      const res = await deleteDutyPerson({ id: person.dutyPersonId, remark })
      if (res.code === 0) {
        ElMessage.success('删除成功')
        await loadConfigFromLocal()
        refreshCalendar()
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    } else {
      ElMessage.error('无法获取值班人员ID')
    }
  }).catch(() => {})
}

const handleRemoveSaturdayDuty = async (group, index) => {
  const person = group === 'group1' ? saturdayGroup1.value[index] : saturdayGroup2.value[index]
  ElMessageBox.prompt('请输入删除原因', '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPlaceholder: '请输入调班原因',
    inputValidator: (value) => {
      if (!value || value.trim().length < 2) return '备注至少需要2个字符'
      if (value.length > 200) return '备注长度不能超过200个字符'
      return true
    },
  }).then(async ({ value: remark }) => {
    if (person.dutyPersonId) {
      const res = await deleteDutyPerson({ id: person.dutyPersonId, remark })
      if (res.code === 0) {
        ElMessage.success('删除成功')
        await loadConfigFromLocal()
        refreshCalendar()
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    } else {
      ElMessage.error('无法获取值班人员ID')
    }
  }).catch(() => {})
}

const handleRemoveMonthEndDuty = async (index) => {
  const person = monthEndDutyList.value[index]
  ElMessageBox.prompt('请输入删除原因', '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPlaceholder: '请输入调班原因',
    inputValidator: (value) => {
      if (!value || value.trim().length < 2) return '备注至少需要2个字符'
      if (value.length > 200) return '备注长度不能超过200个字符'
      return true
    },
  }).then(async ({ value: remark }) => {
    if (person.dutyPersonId) {
      const res = await deleteDutyPerson({ id: person.dutyPersonId, remark })
      if (res.code === 0) {
        ElMessage.success('删除成功')
        await loadConfigFromLocal()
        refreshCalendar()
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    } else {
      ElMessage.error('无法获取值班人员ID')
    }
  }).catch(() => {})
}

// 加载用户列表
const loadUserList = async () => {
  if (!isAdmin.value) return
  loadingUserList.value = true
  try {
    const res = await listUserVOByPage({ current: 1, pageSize: 1000 })
    if (res.code === 0 && res.data) {
      allUsers.value = res.data.records || []
    }
  } catch (error) {
    console.error('加载用户列表失败:', error)
    if (error.response?.status !== 403) {
      ElMessage.error('加载用户列表失败')
    }
  } finally {
    loadingUserList.value = false
  }
}

// 根据用户ID获取用户名
const getUserNameById = (userId) => {
  const user = allUsers.value.find(u => u.id === userId)
  return user ? user.userName : '未知用户'
}

// 移除已选用户
const handleRemoveSelectedUser = (userId) => {
  const index = personForm.userIds.indexOf(userId)
  if (index > -1) {
    personForm.userIds.splice(index, 1)
  }
}

// 提交人员表单
const handleSubmitPerson = async () => {
  personFormRef.value.validate(async (valid) => {
    if (!valid) return
    if (!personForm.userIds || personForm.userIds.length === 0) {
      ElMessage.error('请选择用户')
      return
    }
    submittingPerson.value = true
    try {
      if (dateEditMode.value) {
        let dutyType = editDutyType.value
        let updateConfig = null
        if (dutyType.includes('saturday')) {
          const newPersons = personForm.userIds.map((userId) => {
            const user = allUsers.value.find(u => u.id === userId)
            return user ? { id: user.id, userName: user.userName, dept: user.dept, phone: user.phone } : null
          }).filter(Boolean)
          if (dutyType === 'saturday_group1') {
            saturdayGroup1.value = newPersons
            updateConfig = { saturdayGroup1: newPersons }
          } else {
            saturdayGroup2.value = newPersons
            updateConfig = { saturdayGroup2: newPersons }
          }
        } else if (dutyType === 'monthEnd') {
          const newPersons = personForm.userIds.map((userId) => {
            const user = allUsers.value.find(u => u.id === userId)
            return user ? { id: user.id, userName: user.userName, dept: user.dept, phone: user.phone } : null
          }).filter(Boolean)
          monthEndDutyList.value = newPersons
          updateConfig = { monthEndDutyList: newPersons }
        }
        if (updateConfig) {
          await updateDutyConfigFrom(editStartDate.value, updateConfig, personForm.remark)
          ElMessage.success('排班人员更新成功')
        }
        dateEditMode.value = false
        editStartDate.value = null
        editDutyType.value = null
      } else {
        let dutyType = 'weekday'
        if (currentSaturdayGroup.value) {
          dutyType = currentSaturdayGroup.value === 'group1' ? 'saturday_group1' : 'saturday_group2'
        } else if (addPersonDialogTitle.value.includes('月末')) {
          dutyType = 'month_end'
        }
        let successCount = 0
        let failCount = 0
        const errors = []
        for (const userId of personForm.userIds) {
          try {
            const res = await addDutyPerson({ userId, dutyType, remark: personForm.remark })
            if (res.code === 0) successCount++
            else {
              failCount++
              errors.push(`${getUserNameById(userId)}: ${res.message || '添加失败'}`)
            }
          } catch (error) {
            failCount++
            errors.push(`${getUserNameById(userId)}: 添加失败`)
          }
        }
        if (successCount > 0) ElMessage.success(`成功添加 ${successCount} 个人员`)
        if (failCount > 0) ElMessage.warning(`${failCount} 个人员添加失败${errors.length > 0 ? '\n' + errors.join('\n') : ''}`)
      }
      if (!dateEditMode.value) await loadConfigFromLocal()
      showAddPersonDialog.value = false
      resetPersonForm()
      dateEditMode.value = false
      editStartDate.value = null
      editDutyType.value = null
      await refreshCalendar(false)
      const today = new Date()
      today.setHours(0, 0, 0, 0)
      const pastDateCount = Object.keys(dutyRecordsCache.value || {}).filter(dateStr => new Date(dateStr) < today).length
      console.log('编辑后保留的历史记录天数:', pastDateCount)
    } catch (error) {
      console.error('处理值班人员失败:', error)
      ElMessage.error(isEditMode.value ? '更新值班人员失败' : '添加值班人员失败')
    } finally {
      submittingPerson.value = false
    }
  })
}

// 重置人员表单
const resetPersonForm = () => {
  Object.assign(personForm, { userIds: [], remark: '' })
  personFormRef.value?.clearValidate()
}

// 处理基准日期变更
const handleBaseDateChange = async (newDate) => {
  if (newDate) {
    const res = await saveBaseDate({ baseDate: newDate })
    if (res.code === 0) {
      ElMessage.success('基准日期保存成功')
      await refreshCalendar()
    }
  }
}

// 保存配置
const saveConfigToLocal = async () => {
  try {
    if (baseDate.value) {
      await saveBaseDate({ baseDate: baseDate.value })
    }
  } catch (error) {
    console.error('保存基准日期失败:', error)
  }
}

// 加载配置
const loadConfigFromLocal = async () => {
  try {
    const res = await getDutyConfig()
    if (res.code === 0 && res.data) {
      const config = res.data
      weekdayDutyList.value = config.weekdayDutyList || []
      saturdayGroup1.value = config.saturdayGroup1 || []
      saturdayGroup2.value = config.saturdayGroup2 || []
      monthEndDutyList.value = config.monthEndDutyList || []
      if (config.baseDate) {
        const date = new Date(config.baseDate)
        baseDate.value = date.toISOString().slice(0, 10)
      }
    } else {
      weekdayDutyList.value = []
      saturdayGroup1.value = []
      saturdayGroup2.value = []
      monthEndDutyList.value = []
    }
  } catch (error) {
    console.error('加载配置失败:', error)
  }
}

const handleSaveConfig = async () => {
  try {
    await saveConfigToLocal()
    ElMessage.success('配置保存成功')
    showConfigDialog.value = false
    refreshCalendar()
  } catch (error) {
    console.error('保存配置失败:', error)
    ElMessage.error('保存配置失败')
  }
}

// 生成值班记录
const handleGenerateRecords = async () => {
  if (!generateForm.startDate || !generateForm.endDate) {
    ElMessage.warning('请选择开始日期和结束日期')
    return
  }
  const startDate = new Date(generateForm.startDate)
  const endDate = new Date(generateForm.endDate)
  if (startDate > endDate) {
    ElMessage.warning('开始日期不能晚于结束日期')
    return
  }
  generatingRecords.value = true
  try {
    const res = await generateDutyRecords({
      startDate: generateForm.startDate,
      endDate: generateForm.endDate
    })
    if (res.code === 0) {
      ElMessage.success(`成功生成 ${res.data} 条值班记录`)
      showGenerateDialog.value = false
      generateForm.startDate = ''
      generateForm.endDate = ''
    } else {
      ElMessage.error(res.message || '生成失败')
    }
  } catch (error) {
    console.error('生成值班记录失败:', error)
    ElMessage.error('生成失败')
  } finally {
    generatingRecords.value = false
  }
}

// 新增：根据部门分组
const groupByDept = (persons) => {
  const groups = {}
  persons.forEach(person => {
    const dept = person.dept || '未设置部门'
    if (!groups[dept]) {
      groups[dept] = []
    }
    groups[dept].push(person)
  })
  return groups
}

// 新增：格式化日期头（显示周六/周日和日期）
const formatDateHeader = (dateStr, isSaturday) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const month = date.getMonth() + 1
  const day = date.getDate()
  const weekday = isSaturday ? '周六' : '周日'
  return `${weekday}\n（${month}月${day}日）`
}

// 新增：获取下一天日期
const getNextDay = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  date.setDate(date.getDate() + 1)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}


// 导出详情为PNG
const exportDetailToPng = async () => {
  exportLoading.value = true
  try {
    await nextTick()
    const container = exportContainer.value
    if (!container) {
      ElMessage.error('导出容器不存在')
      return
    }
    const canvas = await html2canvas(container, {
      scale: 2,
      useCORS: true,
      logging: false,
      width: container.offsetWidth,
      height: container.offsetHeight,
      windowWidth: container.scrollWidth,
      windowHeight: container.scrollHeight,
      backgroundColor: '#ffffff'
    })
    const blob = await new Promise((resolve) => canvas.toBlob(resolve, 'image/png'))
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.download = `值班详情_${selectedDayInfo.dateStr}.png`
    a.href = url
    a.click()
    URL.revokeObjectURL(url)
    ElMessage.success('导出成功！')
  } catch (error) {
    console.error('导出PNG失败:', error)
    ElMessage.error('导出失败，请重试')
  } finally {
    exportLoading.value = false
  }
}

// 初始化用户信息
const initUserInfo = () => {
  try {
    const userInfo = localStorage.getItem('userInfo')
    if (userInfo) {
      const user = JSON.parse(userInfo)
      if (!loginUserStore.loginUser.id) {
        loginUserStore.setLoginUser(user)
      }
    }
  } catch (error) {
    console.error('初始化用户信息失败:', error)
  }
}

// 组件挂载时加载数据
onMounted(async () => {
  initUserInfo()
  await loadConfigFromLocal()
  if (isAdmin.value) await loadUserList()
  refreshCalendar()
})

// 监听配置对话框打开，加载用户列表
watch(showConfigDialog, (newVal) => {
  if (newVal && isAdmin.value && allUsers.value.length === 0) {
    loadUserList()
  }
})
</script>

<style scoped>
.home-page {
  width: 100%;
  max-width: 1600px;
  margin: 0 auto;
  padding: 24px;
  background: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e4e7ed;
}

.page-title {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.calendar-container {
  width: 100%;
}

.month-selector {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin-bottom: 24px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
}

.current-month {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.calendar-grid {
  width: 100%;
}

.calendar-weekdays {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 1px;
  background: #e4e7ed;
  border: 1px solid #e4e7ed;
  border-radius: 8px 8px 0 0;
  overflow: hidden;
}

.weekday-header {
  background: #f5f7fa;
  padding: 12px;
  text-align: center;
  font-weight: 600;
  color: #606266;
  font-size: 14px;
}

.calendar-days {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 1px;
  background: #e4e7ed;
  border: 1px solid #e4e7ed;
  border-top: none;
  border-radius: 0 0 8px 8px;
  overflow: hidden;
}

.calendar-day {
  min-height: 120px;
  background: #ffffff;
  padding: 8px;
  display: flex;
  flex-direction: column;
  cursor: pointer;
  transition: all 0.2s;
}

.calendar-day:hover {
  background: #f5f7fa;
  z-index: 1;
}

.calendar-day.other-month {
  background: #fafafa;
  color: #c0c4cc;
}

.calendar-day.today {
  background: #ecf5ff;
  border: 2px solid #409eff;
}

.calendar-day.weekend {
  background: #fef0f0;
}

.calendar-day.weekend.other-month {
  background: #fafafa;
}

.day-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  padding-bottom: 4px;
  border-bottom: 1px solid #f0f0f0;
}

.day-number {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.day-weekday {
  font-size: 12px;
  color: #909399;
}

.day-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.duty-info {
  width: 100%;
}

.duty-person {
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.more-count {
  font-size: 12px;
  color: #909399;
  font-weight: 600;
}

.duty-details {
  font-size: 12px;
  color: #606266;
  line-height: 1.6;
}

.detail-item {
  margin-bottom: 4px;
}

.detail-item .label {
  color: #909399;
}

.detail-item .value {
  color: #303133;
  font-weight: 500;
}

.no-duty {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}

.no-duty-text {
  font-size: 12px;
  color: #c0c4cc;
}

.config-section {
  padding: 16px 0;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-desc {
  color: #909399;
  font-size: 14px;
}

.base-date-config {
  display: flex;
  align-items: center;
  width: 100%;
}

.date-desc {
  color: #909399;
  font-size: 12px;
}

.saturday-group {
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 16px;
  background: #fafafa;
}

.group-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.group-header h4 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .calendar-day {
    min-height: 100px;
  }
  
  .duty-details {
    font-size: 11px;
  }
}

@media (max-width: 768px) {
  .home-page {
    padding: 16px;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .month-selector {
    flex-wrap: wrap;
  }

  .calendar-day {
    min-height: 80px;
    padding: 4px;
  }

  .day-number {
    font-size: 14px;
  }

  .duty-details {
    font-size: 10px;
  }

  .detail-item {
    margin-bottom: 2px;
  }
}

/* 导出容器样式 */
.export-container {
  padding: 40px;
  background: #ffffff;
  border-radius: 8px;
}

.duty-table-wrapper {
  width: 100%;
  max-width: 700px;
  margin: 0 auto;
}

.table-title {
  text-align: center;
  font-size: 26px;
  font-weight: bold;
  margin-bottom: 30px;
  color: #000000;
  letter-spacing: 2px;
}

.duty-export-table {
  width: 100%;
  border-collapse: collapse;
  border: 2.5px solid #000000;
  font-size: 15px;
  background-color: #ffffff !important;
}

.duty-export-table th,
.duty-export-table td {
  border: 1.5px solid #000000;
  padding: 14px 12px;
  text-align: center;
  background-color: #ffffff !important;
  color: #000000;
}

.dept-header {
  width: 140px;
  padding: 18px 12px !important;
  background: #ffffff !important;
  font-weight: bold;
  font-size: 15px;
  text-align: center;
  vertical-align: middle;
  color: #000000;
}

.date-header {
  width: auto;
  font-weight: bold;
  white-space: pre-line;
  line-height: 1.8;
  padding: 18px 20px !important;
  background: #ffffff !important;
  font-size: 15px;
  text-align: center;
  color: #000000;
}


/* 对角线表头样式 */
.diagonal-header {
  position: relative;
  width: 100%;
  height: 70px;
  padding: 0;
}

.diagonal-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  border-bottom: 1.5px solid #000000;
  transform-origin: bottom left;
  transform: skewY(-26.57deg);
}

.header-dept {
  position: absolute;
  left: 15px;
  bottom: 8px;
  font-size: 15px;
  font-weight: bold;
  color: #000000;
}

.header-date {
  position: absolute;
  right: 15px;
  top: 8px;
  font-size: 15px;
  font-weight: bold;
  color: #000000;
}

.date-header {
  width: auto;
  font-weight: bold;
  white-space: pre-line;
  line-height: 1.8;
  padding: 18px 20px !important;
  background: #ffffff !important;
  font-size: 15px;
  text-align: center;
  color: #000000;
}

.dept-cell {
  font-weight: bold;
  vertical-align: middle;
  background-color: #ffffff !important;
  min-width: 120px;
  font-size: 15px;
  color: #000000;
}

.person-cell {
  text-align: left;
  padding: 12px 16px !important;
  background-color: #ffffff !important;
  color: #000000;
}
.person-info-cell {
  display: flex;
  align-items: center;
  justify-content: flex-start;
}



.person-name-text {
  font-weight: 500;
  margin-right: 4px;
  font-size: 15px;
  color: #000000;
}

.person-phone-text {
  font-weight: normal;
  font-size: 15px;
  color: #000000;
}

.empty-cell {
  color: #666666;
  font-size: 16px;
  background-color: #ffffff !important;
}

.empty-message {
  padding: 24px !important;
  color: #999999;
  background-color: #ffffff !important;
}


.person-name {
  font-weight: bold;
  margin-right: 4px;
  font-size: 15px;
  color: #000000;
}

.person-phone {
  font-size: 15px;
  color: #000000;
}

.empty-message {
  padding: 24px !important;
  color: #999999;
  background-color: #ffffff !important;
}

/* 已选用户列表样式 */
.selected-users-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 8px;
  background: #f5f7fa;
  border-radius: 4px;
  min-height: 40px;
}
</style>