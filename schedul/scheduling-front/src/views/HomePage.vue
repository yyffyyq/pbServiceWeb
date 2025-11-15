<template>
  <div class="home-page">
    <div class="page-header">
      <h1 class="page-title">值班安排表</h1>
      <div class="header-actions" v-if="isAdmin">
        <el-button :icon="Setting" @click="showConfigDialog = true">配置管理</el-button>
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
        <el-form-item label="选择用户" prop="userId">
          <el-select
            v-model="personForm.userId"
            placeholder="请选择用户"
            filterable
            style="width: 100%;"
            @change="handleUserSelect"
            :loading="loadingUserList"
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
        <el-form-item label="姓名">
          <el-input
            v-model="personForm.userName"
            disabled
            placeholder="选择用户后自动填充"
          />
        </el-form-item>
        <el-form-item label="部门">
          <el-input
            v-model="personForm.dept"
            disabled
            placeholder="选择用户后自动填充"
          />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input
            v-model="personForm.phone"
            disabled
            placeholder="选择用户后自动填充"
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

    <!-- 查看日期详情对话框 -->
    <el-dialog
      v-model="showDayDetailDialog"
      title="值班人员详情"
      width="600px"
    >
      <div class="day-detail">
        <div class="detail-date">
          <el-icon><Calendar /></el-icon>
          <span>{{ selectedDayInfo.dateStr }}</span>
          <el-tag :type="selectedDayInfo.isWeekend ? 'warning' : 'primary'" size="small" style="margin-left: 8px;">
            {{ selectedDayInfo.weekdayName }}
          </el-tag>
        </div>
        <div class="detail-persons" v-if="selectedDayInfo.dutyPersons && selectedDayInfo.dutyPersons.length > 0">
          <div
            v-for="(person, index) in selectedDayInfo.dutyPersons"
            :key="index"
            class="person-card"
          >
            <div class="person-header">
              <span class="person-name">{{ person.userName }}</span>
              <el-tag size="small" type="info">{{ person.dept || '未设置部门' }}</el-tag>
            </div>
            <div class="person-info">
              <div class="info-item">
                <el-icon><Phone /></el-icon>
                <span>{{ person.phone || '未设置手机号' }}</span>
              </div>
            </div>
          </div>
        </div>
        <el-empty v-else description="该日期未安排值班人员" />
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
import { ref, reactive, computed, onMounted, watch } from 'vue'
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
  Phone
} from '@element-plus/icons-vue'
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
  deleteUser
} from '@/api/index'
import { useLoginUserStore } from '@/stores/useLoginUserStore'

// 初始化 Pinia Store
const loginUserStore = useLoginUserStore()

// 响应式数据
const currentDate = ref(new Date().toISOString().slice(0, 7)) // YYYY-MM
const showConfigDialog = ref(false)

// 判断是否为管理员
const isAdmin = computed(() => {
  // 优先从 store 获取
  if (loginUserStore.loginUser && loginUserStore.loginUser.userRole === 'admin') {
    return true
  }
  // 如果 store 中没有，从 localStorage 获取
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
const editIndex = ref(-1) // 编辑模式下的索引
const isEditMode = computed(() => editIndex.value >= 0)
const loadingUsers = ref(false)
const loadingUserList = ref(false)
const submittingPerson = ref(false)

// 用户列表
const allUsers = ref([])
// 允许同一用户添加多次，所以不再过滤已配置的用户
const availableUsers = computed(() => {
  return allUsers.value
})

// 检查用户是否已经配置过（用于显示标记）
const isUserAlreadyConfigured = (userId) => {
  // 检查工作日列表
  if (weekdayDutyList.value.some(config => config.id === userId)) {
    return true
  }
  // 检查周六单周组
  if (saturdayGroup1.value.some(config => config.id === userId)) {
    return true
  }
  // 检查周六双周组
  if (saturdayGroup2.value.some(config => config.id === userId)) {
    return true
  }
  // 检查月末值班列表
  if (monthEndDutyList.value.some(config => config.id === userId)) {
    return true
  }
  return false
}

// 星期标题
const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']

// 配置管理标签页
const configTab = ref('weekday')

// 工作日值班人员列表（周一到周五，固定值班）
const weekdayDutyList = ref([])

// 周六值班人员：单周组和双周组
const saturdayGroup1 = ref([]) // 单周组
const saturdayGroup2 = ref([]) // 双周组

// 月末值班人员列表（每月最后两天）
const monthEndDutyList = ref([])

// 基准日期（用于判断单周/双周，该日期所在周为单周）
const baseDate = ref(new Date().toISOString().slice(0, 10)) // YYYY-MM-DD

// 当前添加的周六组类型
const currentSaturdayGroup = ref('group1')

// 选中的日期信息（用于详情对话框）
const selectedDayInfo = reactive({
  dateStr: '',
  weekdayName: '',
  isWeekend: false,
  dutyPersons: []
})

// 人员表单数据
const personForm = reactive({
  userId: null,
  userName: '',
  dept: '',
  phone: ''
})

// 表单验证规则
const personFormRules = {
  userId: [
    { required: true, message: '请选择用户', trigger: 'change' }
  ]
}

// 计算当前月份的所有日期
const calendarDays = computed(() => {
  const year = parseInt(currentDate.value.split('-')[0])
  const month = parseInt(currentDate.value.split('-')[1]) - 1
  const firstDay = new Date(year, month, 1)
  const lastDay = new Date(year, month + 1, 0)
  const firstDayWeek = firstDay.getDay() // 0-6, 0是周日
  const daysInMonth = lastDay.getDate()
  
  const days = []
  const today = new Date()
  today.setHours(0, 0, 0, 0)

  // 添加上个月的最后几天（用于填充第一周）
  for (let i = firstDayWeek - 1; i >= 0; i--) {
    const date = new Date(year, month, -i)
    days.push(createDayData(date, true))
  }

  // 添加当月的所有日期
  for (let day = 1; day <= daysInMonth; day++) {
    const date = new Date(year, month, day)
    days.push(createDayData(date, false))
  }

  // 添加下个月的前几天（用于填充最后一周，使总数为7的倍数）
  const remainingDays = 42 - days.length // 6行 x 7天 = 42
  for (let day = 1; day <= remainingDays; day++) {
    const date = new Date(year, month + 1, day)
    days.push(createDayData(date, true))
  }

  return days
})

// 判断指定日期是单周还是双周
const isSingleWeek = (date) => {
  if (!baseDate.value) return true
  
  const base = new Date(baseDate.value)
  base.setHours(0, 0, 0, 0)
  const target = new Date(date)
  target.setHours(0, 0, 0, 0)
  
  // 计算两个日期之间的周数差
  const diffTime = target.getTime() - base.getTime()
  const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24))
  const diffWeeks = Math.floor(diffDays / 7)
  
  // 如果周数差是偶数，则是单周；如果是奇数，则是双周
  return diffWeeks % 2 === 0
}

// 判断是否为月末最后两天
const isMonthEndDay = (date) => {
  const year = date.getFullYear()
  const month = date.getMonth()
  const day = date.getDate()
  
  // 获取该月的最后一天
  const lastDay = new Date(year, month + 1, 0).getDate()
  
  // 判断是否为最后一天或倒数第二天
  return day === lastDay || day === lastDay - 1
}

// 创建日期数据对象
const createDayData = (date) => {
  const dayOfWeek = date.getDay()
  const isWeekend = dayOfWeek === 0 || dayOfWeek === 6 // 周日或周六
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const isToday = date.getTime() === today.getTime()
  const isOtherMonth = false

  // 计算值班人员
  let dutyPersons = []
  
  // 首先检查是否为月末最后两天
  if (isMonthEndDay(date)) {
    // 月末最后两天：显示月末值班人员
    if (monthEndDutyList.value && Array.isArray(monthEndDutyList.value) && monthEndDutyList.value.length > 0) {
      dutyPersons = monthEndDutyList.value.map(config => ({
        id: config.id,
        userName: config.userName,
        dept: config.dept || '',
        phone: config.phone || ''
      }))
    }
  } else if (dayOfWeek >= 1 && dayOfWeek <= 5) {
    // 周一到周五：显示所有工作日值班人员
    if (weekdayDutyList.value && Array.isArray(weekdayDutyList.value) && weekdayDutyList.value.length > 0) {
      dutyPersons = weekdayDutyList.value.map(config => ({
        id: config.id,
        userName: config.userName,
        dept: config.dept || '',
        phone: config.phone || ''
      }))
    }
  } else if (dayOfWeek === 6) {
    // 周六：根据单周/双周显示对应组的值班人员
    const isSingle = isSingleWeek(date)
    const saturdayGroup = isSingle ? saturdayGroup1.value : saturdayGroup2.value
    if (saturdayGroup && Array.isArray(saturdayGroup) && saturdayGroup.length > 0) {
      dutyPersons = saturdayGroup.map(config => ({
        id: config.id,
        userName: config.userName,
        dept: config.dept || '',
        phone: config.phone || ''
      }))
    }
  }
  // 周日不安排值班

  return {
    date,
    isOtherMonth,
    isWeekend,
    isToday,
    dutyPersons
  }
}

// 获取星期名称
const getWeekdayName = (date) => {
  return weekdays[date.getDay()]
}

// 上一个月
const prevMonth = () => {
  const date = new Date(currentDate.value + '-01')
  date.setMonth(date.getMonth() - 1)
  currentDate.value = date.toISOString().slice(0, 7)
  refreshCalendar()
}

// 下一个月
const nextMonth = () => {
  const date = new Date(currentDate.value + '-01')
  date.setMonth(date.getMonth() + 1)
  currentDate.value = date.toISOString().slice(0, 7)
  refreshCalendar()
}

// 月份变化
const handleMonthChange = () => {
  refreshCalendar()
}

// 刷新日历
const refreshCalendar = () => {
  // 重新计算日历数据
  ElMessage.success('日历已刷新')
}

// 获取星期名称（根据索引）
const getWeekdayNameByIndex = (dayIndex) => {
  return weekdays[dayIndex]
}

// 点击日期
const handleDayClick = (day) => {
  if (day.isOtherMonth) return
  
  // 格式化日期字符串
  const year = day.date.getFullYear()
  const month = String(day.date.getMonth() + 1).padStart(2, '0')
  const date = String(day.date.getDate()).padStart(2, '0')
  
  Object.assign(selectedDayInfo, {
    dateStr: `${year}-${month}-${date}`,
    weekdayName: getWeekdayName(day.date),
    isWeekend: day.isWeekend,
    dutyPersons: day.dutyPersons || []
  })
  
  showDayDetailDialog.value = true
}

// 添加工作日值班人员
const handleAddWeekdayDuty = () => {
  currentSaturdayGroup.value = null
  addPersonDialogTitle.value = '添加工作日值班人员'
  resetPersonForm()
  showAddPersonDialog.value = true
}

// 添加周六值班人员
const handleAddSaturdayDuty = (group) => {
  currentSaturdayGroup.value = group
  addPersonDialogTitle.value = `添加周六${group === 'group1' ? '单周' : '双周'}组值班人员`
  resetPersonForm()
  showAddPersonDialog.value = true
}

// 删除工作日值班人员
const handleRemoveWeekdayDuty = async (index) => {
  ElMessageBox.confirm('确定要删除该值班人员吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    const person = weekdayDutyList.value[index]
    if (person.dutyPersonId) {
      try {
        const res = await deleteDutyPerson({ id: person.dutyPersonId })
        if (res.code === 0) {
          ElMessage.success('删除成功')
          // 重新加载配置
          await loadConfigFromLocal()
          refreshCalendar()
        } else {
          ElMessage.error(res.message || '删除失败')
        }
      } catch (error) {
        console.error('删除失败:', error)
        ElMessage.error('删除失败')
      }
    } else {
      ElMessage.error('无法获取值班人员ID')
    }
  }).catch(() => {})
}

// 删除周六值班人员
const handleRemoveSaturdayDuty = async (group, index) => {
  ElMessageBox.confirm('确定要删除该值班人员吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    const person = group === 'group1' ? saturdayGroup1.value[index] : saturdayGroup2.value[index]
    if (person.dutyPersonId) {
      try {
        const res = await deleteDutyPerson({ id: person.dutyPersonId })
        if (res.code === 0) {
          ElMessage.success('删除成功')
          // 重新加载配置
          await loadConfigFromLocal()
          refreshCalendar()
        } else {
          ElMessage.error(res.message || '删除失败')
        }
      } catch (error) {
        console.error('删除失败:', error)
        ElMessage.error('删除失败')
      }
    } else {
      ElMessage.error('无法获取值班人员ID')
    }
  }).catch(() => {})
}

// 添加月末值班人员
const handleAddMonthEndDuty = () => {
  currentSaturdayGroup.value = null
  addPersonDialogTitle.value = '添加月末值班人员'
  resetPersonForm()
  showAddPersonDialog.value = true
}

// 删除月末值班人员
const handleRemoveMonthEndDuty = async (index) => {
  ElMessageBox.confirm('确定要删除该值班人员吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    const person = monthEndDutyList.value[index]
    if (person.dutyPersonId) {
      try {
        const res = await deleteDutyPerson({ id: person.dutyPersonId })
        if (res.code === 0) {
          ElMessage.success('删除成功')
          // 重新加载配置
          await loadConfigFromLocal()
          refreshCalendar()
        } else {
          ElMessage.error(res.message || '删除失败')
        }
      } catch (error) {
        console.error('删除失败:', error)
        ElMessage.error('删除失败')
      }
    } else {
      ElMessage.error('无法获取值班人员ID')
    }
  }).catch(() => {})
}

// 加载用户列表（仅管理员）
const loadUserList = async () => {
  // 检查是否为管理员
  if (!isAdmin.value) {
    console.warn('非管理员用户无法加载用户列表')
    return
  }

  loadingUserList.value = true
  try {
    const res = await listUserVOByPage({
      current: 1,
      pageSize: 1000 // 获取所有用户
    })
    if (res.code === 0 && res.data) {
      allUsers.value = res.data.records || []
    }
  } catch (error) {
    console.error('加载用户列表失败:', error)
    // 如果是权限错误，不显示错误提示（因为只有管理员才能看到配置对话框）
    if (error.response?.status === 403 || error.response?.data?.code === 40300) {
      console.warn('权限不足，无法加载用户列表')
    } else {
      ElMessage.error('加载用户列表失败')
    }
  } finally {
    loadingUserList.value = false
  }
}

// 根据用户ID获取完整用户信息（仅管理员）
const getUserFullInfo = async (userId) => {
  // 检查是否为管理员
  if (!isAdmin.value) {
    console.warn('非管理员用户无法获取完整用户信息')
    return null
  }

  try {
    const res = await getUserById(userId)
    if (res.code === 0 && res.data) {
      return res.data
    }
    return null
  } catch (error) {
    console.error('获取用户信息失败:', error)
    // 如果是权限错误，静默处理（因为只有管理员才能看到配置对话框）
    if (error.response?.status === 403 || error.response?.data?.code === 40300) {
      console.warn('权限不足，无法获取用户完整信息')
    }
    return null
  }
}

// 用户选择处理
const handleUserSelect = async (userId) => {
  if (!userId) {
    Object.assign(personForm, {
      userName: '',
      dept: '',
      phone: ''
    })
    return
  }

  // 先从本地用户列表查找
  const localUser = allUsers.value.find(u => u.id === userId)
  if (localUser) {
    // 如果本地有，尝试获取完整信息（包含 phone 和 dept）
    const fullInfo = await getUserFullInfo(userId)
    if (fullInfo) {
      personForm.userName = fullInfo.userName || localUser.userName
      personForm.dept = fullInfo.dept || ''
      personForm.phone = fullInfo.phone || ''
    } else {
      personForm.userName = localUser.userName
      personForm.dept = ''
      personForm.phone = ''
    }
  }
}

// 提交人员表单
const handleSubmitPerson = async () => {
  personFormRef.value.validate(async (valid) => {
    if (!valid) return

    if (!personForm.userId) {
      ElMessage.error('请选择用户')
      return
    }

    submittingPerson.value = true
    try {
      // 获取完整用户信息
      const fullInfo = await getUserFullInfo(personForm.userId)
      if (!fullInfo) {
        ElMessage.error('获取用户信息失败')
        return
      }

      // 确定值班类型
      let dutyType = 'weekday'
      if (currentSaturdayGroup.value) {
        dutyType = currentSaturdayGroup.value === 'group1' ? 'saturday_group1' : 'saturday_group2'
      } else if (addPersonDialogTitle.value.includes('月末')) {
        dutyType = 'month_end'
      }

      // 调用后端接口添加值班人员
      const res = await addDutyPerson({
        userId: personForm.userId,
        dutyType: dutyType
      })

      if (res.code === 0) {
        ElMessage.success('添加成功')
        // 重新加载配置
        await loadConfigFromLocal()
        showAddPersonDialog.value = false
        resetPersonForm()
        refreshCalendar()
      } else {
        ElMessage.error(res.message || '添加失败')
      }
    } catch (error) {
      console.error('保存值班人员失败:', error)
      ElMessage.error('保存失败')
    } finally {
      submittingPerson.value = false
    }
  })
}

// 重置人员表单
const resetPersonForm = () => {
  Object.assign(personForm, {
    userId: null,
    userName: '',
    dept: '',
    phone: ''
  })
  personFormRef.value?.clearValidate()
}

// 保存基准日期到后端
const saveConfigToLocal = async () => {
  try {
    if (baseDate.value) {
      const res = await saveBaseDate({ baseDate: baseDate.value })
      if (res.code === 0) {
        console.log('保存基准日期成功')
      }
    }
  } catch (error) {
    console.error('保存基准日期失败:', error)
  }
}

// 从后端加载配置
const loadConfigFromLocal = async () => {
  try {
    const res = await getDutyConfig()
    if (res.code === 0 && res.data) {
      const config = res.data
      console.log('加载值班配置:', config)
      
      // 加载工作日值班人员
      weekdayDutyList.value = config.weekdayDutyList || []
      console.log('工作日值班人员:', weekdayDutyList.value.length, '人')
      
      // 加载周六单周组
      saturdayGroup1.value = config.saturdayGroup1 || []
      console.log('周六单周组:', saturdayGroup1.value.length, '人')
      
      // 加载周六双周组
      saturdayGroup2.value = config.saturdayGroup2 || []
      console.log('周六双周组:', saturdayGroup2.value.length, '人')
      
      // 加载月末值班人员
      monthEndDutyList.value = config.monthEndDutyList || []
      console.log('月末值班人员:', monthEndDutyList.value.length, '人')
      
      // 加载基准日期
      if (config.baseDate) {
        // 将日期字符串转换为 YYYY-MM-DD 格式
        const date = new Date(config.baseDate)
        baseDate.value = date.toISOString().slice(0, 10)
        console.log('基准日期:', baseDate.value)
      }
    } else {
      console.log('未找到值班配置')
      // 初始化空数组
      weekdayDutyList.value = []
      saturdayGroup1.value = []
      saturdayGroup2.value = []
      monthEndDutyList.value = []
    }
  } catch (error) {
    console.error('加载配置失败:', error)
    // 确保即使出错也初始化空数组
    weekdayDutyList.value = weekdayDutyList.value || []
    saturdayGroup1.value = saturdayGroup1.value || []
    saturdayGroup2.value = saturdayGroup2.value || []
    monthEndDutyList.value = monthEndDutyList.value || []
  }
}

// 保存配置
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

// 删除值班人员
const handleRemoveDutyPerson = (index) => {
  ElMessageBox.confirm('确定要删除该值班人员吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    dutyConfigList.value.splice(index, 1)
    saveConfigToLocal()
    ElMessage.success('删除成功')
    refreshCalendar()
  }).catch(() => {})
}

// 监听配置对话框打开，加载用户列表（仅管理员）
watch(showConfigDialog, (newVal) => {
  if (newVal && isAdmin.value && allUsers.value.length === 0) {
    loadUserList()
  }
})

// 初始化用户信息到 store
const initUserInfo = () => {
  try {
    const userInfo = localStorage.getItem('userInfo')
    if (userInfo) {
      const user = JSON.parse(userInfo)
      // 如果 store 中没有用户信息，从 localStorage 同步
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
  // 先初始化用户信息
  initUserInfo()
  
  // 所有用户都需要加载配置（用于显示值班安排）
  await loadConfigFromLocal()
  
  // 只有管理员才加载用户列表（用于配置管理）
  if (isAdmin.value) {
    await loadUserList()
  }
  
  refreshCalendar()
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

.section-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
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

/* 日期详情对话框样式 */
.day-detail {
  padding: 16px 0;
}

.detail-date {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e4e7ed;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.detail-persons {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.person-card {
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
}

.person-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.person-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.person-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #606266;
  font-size: 14px;
}
</style>
