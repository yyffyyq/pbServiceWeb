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
              @change="loadDutyRecords"
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
          <div v-for="day in weekdays" :key="day" class="weekday-header">{{ day }}</div>
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

    <DutyConfigDialog
        v-model="showConfigDialog"
        :config-data="allConfigs"
        :base-date="baseDate"
        @add-person="onOpenPersonSelect"
        @save-config="handleSaveConfig"
        @base-date-change="handleBaseDateChange"
        @request-reload="loadAllConfigs"
    />

    <PersonSelectDialog
        v-model="showPersonSelect"
        :title="personSelectTitle"
        :initial-user-ids="initialSelectedIds"
        :edit-mode="dateEditMode"
        @submit-success="onPersonSelectConfirm"
    />

    <DutyGenerateDialog v-model="showGenerateDialog" @success="refreshCalendar" />

    <DayDetailDialog
        v-model="showDayDetail"
        :info="selectedDayInfo"
        :is-admin="isAdmin"
        @edit-click="handleEditDayDuty"
        @set-holiday="handleSetHoliday"
        @clear-holiday="handleClearHoliday"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { Setting, DocumentAdd, ArrowLeft, ArrowRight, Refresh } from '@element-plus/icons-vue';

import { addDutyPerson, deleteDutyPerson, updateDutyConfigFrom,setHoliday,deleteHoliday } from '@/api/index';
import { initHolidayConfig } from '@/utils/holiday';
import { useLoginUserStore } from '@/stores/useLoginUserStore';

import { useCalendar } from './hooks/useCalendar';
import { useDutyConfig } from './hooks/useDutyConfig';

import DutyConfigDialog from './components/DutyConfigDialog.vue';
import PersonSelectDialog from './components/PersonSelectDialog.vue';
import DutyGenerateDialog from './components/DutyGenerateDialog.vue';
import DayDetailDialog from './components/DayDetailDialog.vue';

const loginUserStore = useLoginUserStore();
const isAdmin = computed(() => {
  if (loginUserStore.loginUser && loginUserStore.loginUser.userRole === 'admin') return true;
  try {
    const userInfo = localStorage.getItem('userInfo');
    return userInfo ? JSON.parse(userInfo).userRole === 'admin' : false;
  } catch (error) {}
  return false;
});

const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
const { currentDate, dutyRecordsCache, generateCalendarDays, loadDutyRecords, prevMonth, nextMonth } = useCalendar();
const { allConfigs, baseDate, loadAllConfigs, updateBaseDate } = useDutyConfig();

const calendarDays = computed(() => {
  const trigger = dutyRecordsCache.value;
  return generateCalendarDays(baseDate.value, allConfigs);
});

const showConfigDialog = ref(false);
const showPersonSelect = ref(false);
const showGenerateDialog = ref(false);
const showDayDetail = ref(false);

const personSelectTitle = ref('');
const initialSelectedIds = ref([]);
const currentAddingType = ref('');
const dateEditMode = ref(false);
const editStartDate = ref(null);
const editDutyType = ref(null);

const selectedDayInfo = reactive({ dateStr: '', dutyPersons: [], isEditable: false, isSaturday: false, isMonthEnd: false, date: null });

const refreshCalendar = async () => {
  await loadDutyRecords();
  ElMessage.success('日历已刷新');
};

const handleDayClick = (day) => {
  if (day.isOtherMonth) return;
  const isSat = day.date.getDay() === 6;
  const isEnd = day.date.getDate() >= new Date(day.date.getFullYear(), day.date.getMonth() + 1, 0).getDate() - 1;
  Object.assign(selectedDayInfo, { ...day, isEditable: isSat || isEnd, isSaturday: isSat, isMonthEnd: isEnd });
  showDayDetail.value = true;
};

const onOpenPersonSelect = (type) => {
  currentAddingType.value = type;
  dateEditMode.value = false;
  const titles = { weekday: '编辑工作日排班人员', group1: '编辑周六单周排班', group2: '编辑周六双周排班', monthEnd: '编辑月末排班人员' };
  personSelectTitle.value = titles[type];
  const listMap = { weekday: 'weekdayDutyList', group1: 'saturdayGroup1', group2: 'saturdayGroup2', monthEnd: 'monthEndDutyList' };
  initialSelectedIds.value = allConfigs[listMap[type]].map(p => p.id);
  showPersonSelect.value = true;
};

const handleEditDayDuty = () => {
  dateEditMode.value = true;
  editStartDate.value = selectedDayInfo.dateStr;

  if (selectedDayInfo.isSaturday) {
    const isSingle = selectedDayInfo.weekType === '单周';
    editDutyType.value = isSingle ? 'saturday_group1' : 'saturday_group2';
  } else if (selectedDayInfo.isMonthEnd) {
    editDutyType.value = 'monthEnd';
  }

  personSelectTitle.value = selectedDayInfo.isSaturday ? `编辑${selectedDayInfo.dateStr}（周六）排班` : `编辑${selectedDayInfo.dateStr}（月末）排班`;
  initialSelectedIds.value = selectedDayInfo.dutyPersons.map(p => p.id);
  showPersonSelect.value = true;
};

const onPersonSelectConfirm = async ({ userIds, selectedUsers, remark }) => {
  if (dateEditMode.value) {
    let updateConfig = null;
    if (editDutyType.value === 'saturday_group1') updateConfig = { saturdayGroup1: selectedUsers };
    else if (editDutyType.value === 'saturday_group2') updateConfig = { saturdayGroup2: selectedUsers };
    else if (editDutyType.value === 'monthEnd') updateConfig = { monthEndDutyList: selectedUsers };

    if (updateConfig) {
      await updateDutyConfigFrom(editStartDate.value, updateConfig, remark);
      ElMessage.success('更新成功');
    }
  } else {
    const typeMap = { weekday: 'weekday', group1: 'saturday_group1', group2: 'saturday_group2', monthEnd: 'month_end' };
    const dutyType = typeMap[currentAddingType.value];
    const listMap = { weekday: 'weekdayDutyList', group1: 'saturdayGroup1', group2: 'saturdayGroup2', monthEnd: 'monthEndDutyList' };

    const existingList = allConfigs[listMap[currentAddingType.value]];
    const existingIds = existingList.map(p => p.id);

    const newIdsToAdd = userIds.filter(id => !existingIds.includes(id));
    const removedPersons = existingList.filter(p => !userIds.includes(p.id));

    let successCount = 0, removeCount = 0;
    for (const userId of newIdsToAdd) {
      try { const res = await addDutyPerson({ userId, dutyType, remark }); if (res.code === 0) successCount++; } catch (e) {}
    }
    for (const person of removedPersons) {
      if (person.dutyPersonId) {
        try { const res = await deleteDutyPerson({ id: person.dutyPersonId, remark: remark || '批量取消' }); if (res.code === 0) removeCount++; } catch (e) {}
      }
    }
    if (successCount > 0 || removeCount > 0) ElMessage.success(`新增 ${successCount} 人，移除 ${removeCount} 人`);
  }

  await loadAllConfigs();
  await loadDutyRecords();
};

// =================================设置单日为休息日操作==========================================================
// 设为放假
const handleSetHoliday = async (dateStr) => {
  try {
    const res = await setHoliday({ holidayDate: dateStr });
    if (res.code === 0) {
      ElMessage.success(`${dateStr} 已设为放假`);
      showDayDetail.value = false;
      await initHolidayConfig(); // 重新加载全局节假日配置
      await loadDutyRecords();   // 刷新日历数据触发响应式更新
    }
  } catch (error) {
    ElMessage.error('设置失败');
  }
};

// 取消放假 (清空)
const handleClearHoliday = async (dateStr) => {
  try {
    const res = await deleteHoliday({ holidayDate: dateStr });
    if (res.code === 0) {
      ElMessage.success(`${dateStr} 放假已取消`);
      showDayDetail.value = false;
      await initHolidayConfig(); // 重新加载全局节假日配置
      await loadDutyRecords();   // 刷新日历数据触发响应式更新
    }
  } catch (error) {
    ElMessage.error('取消失败');
  }
};


// =====================================END====================================================================


const handleBaseDateChange = async (val) => {
  const success = await updateBaseDate(val);
  if (success) await loadDutyRecords();
};

const handleSaveConfig = () => {
  showConfigDialog.value = false;
  refreshCalendar();
};

const initUserInfo = () => {
  try {
    const userInfo = localStorage.getItem('userInfo');
    if (userInfo) loginUserStore.setLoginUser(JSON.parse(userInfo));
  } catch (e) {}
};

onMounted(async () => {
  await initHolidayConfig();
  initUserInfo();
  await loadAllConfigs();
  await loadDutyRecords();
});
</script>

<style scoped>
/* 100% 保留你原文件的 Apple-Theme 样式 */
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
}
.home-page { font-family: -apple-system, BlinkMacSystemFont, "SF Pro Text", "PingFang SC", "Helvetica Neue", Arial, sans-serif; width: 100%; max-width: 1400px; margin: 0 auto; padding: 32px; background-color: var(--apple-bg); min-height: 100vh; box-sizing: border-box; }
.page-header { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 24px; padding: 0 12px; }
.header-content .page-title { margin: 0; font-size: 32px; font-weight: 700; color: var(--apple-text-main); letter-spacing: -0.5px; }
.header-content .page-subtitle { margin: 4px 0 0 0; font-size: 14px; font-weight: 500; color: var(--apple-text-sub); text-transform: uppercase; letter-spacing: 1px; }
.header-actions { display: flex; gap: 12px; }
.apple-btn { border-radius: var(--apple-radius-sm); font-weight: 500; border: 1px solid var(--apple-border); box-shadow: 0 1px 2px rgba(0,0,0,0.02); transition: all 0.2s cubic-bezier(0.25, 0.8, 0.25, 1); }
.apple-btn:not(.primary):hover { background-color: #f0f0f2; border-color: #d1d1d6; }
.apple-btn.primary { background-color: var(--apple-blue); border: none; color: white; }
.apple-btn.primary:hover { background-color: var(--apple-blue-hover); transform: translateY(-1px); box-shadow: 0 4px 12px rgba(0, 122, 255, 0.3); }
.apple-btn.primary-light { background-color: var(--apple-blue-light); color: var(--apple-blue); border: none; }
.apple-btn.success { background-color: #34c759; border: none; color: white; }
.apple-btn.success:hover { background-color: #2eb350; }
.apple-icon-btn { border: none; background: var(--apple-bg); color: var(--apple-text-main); }
.apple-icon-btn:hover { background: #e5e5ea; }
.calendar-card { background: var(--apple-card); border-radius: var(--apple-radius-lg); box-shadow: var(--apple-shadow-md); padding: 24px; border: 1px solid rgba(0,0,0,0.02); }
.month-selector { display: flex; align-items: center; justify-content: center; gap: 12px; margin-bottom: 24px; }
.current-month-wrapper { background: var(--apple-bg); border-radius: var(--apple-radius-sm); padding: 2px 4px; }
:deep(.apple-date-picker .el-input__wrapper) { background: transparent; border: none !important; box-shadow: none !important; }
:deep(.apple-date-picker .el-input__inner) { font-size: 17px; font-weight: 600; color: var(--apple-text-main); text-align: center; }
.divider { width: 1px; height: 24px; background-color: var(--apple-border); margin: 0 12px; }
.calendar-grid { border-radius: var(--apple-radius-md); border: 1px solid var(--apple-border); overflow: hidden; background: var(--apple-border); }
.calendar-weekdays { display: grid; grid-template-columns: repeat(7, 1fr); background: var(--apple-card); gap: 1px; border-bottom: 1px solid var(--apple-border); }
.weekday-header { padding: 12px 0; text-align: center; font-size: 13px; font-weight: 600; color: var(--apple-text-sub); background: #fafafa; }
.calendar-days { display: grid; grid-template-columns: repeat(7, 1fr); gap: 1px; }
.calendar-day { background: var(--apple-card); min-height: 120px; padding: 10px; display: flex; flex-direction: column; transition: background 0.2s ease; cursor: pointer; }
.calendar-day:hover { background: #f9f9fb; }
.calendar-day.other-month { background: #fdfdfd; opacity: 0.6; }
.calendar-day.weekend { background: #fefcfc; }
.day-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 8px; }
.day-number-wrapper { display: inline-flex; align-items: center; justify-content: center; width: 28px; height: 28px; border-radius: 50%; }
.day-number { font-size: 15px; font-weight: 500; color: var(--apple-text-main); }
.calendar-day.today .day-number-wrapper { background-color: var(--apple-blue); }
.calendar-day.today .day-number { color: #ffffff; font-weight: 700; }
.day-tags { display: flex; gap: 4px; }
.apple-tag { font-size: 10px; font-weight: 600; padding: 2px 6px; border-radius: 4px; line-height: 1.2; }
.holiday-tag { background: #ffebeb; color: var(--apple-red); }
.workday-tag { background: var(--apple-blue-light); color: var(--apple-blue); }
.week-type-tag { background: #f0f0f2; color: var(--apple-text-sub); }
.day-content { flex: 1; display: flex; flex-direction: column; }
.duty-pills { display: flex; flex-direction: column; gap: 4px; margin-bottom: 4px; }
.duty-pill { display: inline-flex; align-items: center; background: var(--apple-blue-light); color: var(--apple-blue); border-radius: 6px; padding: 4px 8px; font-size: 12px; font-weight: 600; }
.duty-pill.is-weekend-pill { background: #fff0eb; color: #ff9500; }
.pill-count { margin-left: 4px; opacity: 0.7; }
.duty-details-mini { margin-top: auto; }
.detail-text { font-size: 11px; color: var(--apple-text-sub); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.no-duty { height: 100%; display: flex; align-items: center; justify-content: center; }
.no-duty-text { font-size: 12px; color: #d1d1d6; font-weight: 500; }

:deep(.apple-dialog) { border-radius: var(--apple-radius-lg); box-shadow: var(--apple-shadow-lg); overflow: hidden; }
:deep(.apple-dialog .el-dialog__header) { margin: 0; padding: 20px 24px; background: rgba(245, 245, 247, 0.8); backdrop-filter: blur(20px); border-bottom: 1px solid var(--apple-border); }
:deep(.apple-dialog .el-dialog__title) { font-weight: 600; font-size: 17px; color: var(--apple-text-main); }
:deep(.apple-dialog .el-dialog__body) { padding: 24px; }
:deep(.apple-dialog .el-dialog__footer) { padding: 16px 24px; border-top: 1px solid var(--apple-border); background: #fafafa; }
.apple-footer { display: flex; justify-content: flex-end; gap: 12px; }

:deep(.apple-tabs .el-tabs__item) { font-weight: 500; font-size: 15px; }
:deep(.apple-tabs .el-tabs__item.is-active) { font-weight: 600; }
:deep(.apple-tabs .el-tabs__nav-wrap::after) { height: 1px; background-color: var(--apple-border); }
:deep(.apple-table) { --el-table-border-color: var(--apple-border); --el-table-header-bg-color: #f5f5f7; border-radius: var(--apple-radius-sm); overflow: hidden; }
:deep(.apple-table th.el-table__cell) { font-weight: 600; color: var(--apple-text-main); background-color: #f5f5f7; }

:deep(.apple-form .el-form-item__label) { font-weight: 600; color: var(--apple-text-main); padding-bottom: 6px; }
:deep(.apple-textarea .el-textarea__inner), :deep(.apple-cascader .el-input__wrapper) { border-radius: var(--apple-radius-sm); box-shadow: 0 0 0 1px var(--apple-border) inset; background: #fafafa; transition: all 0.2s; }
:deep(.apple-textarea .el-textarea__inner:focus), :deep(.apple-cascader .el-input__wrapper.is-focus) { box-shadow: 0 0 0 2px var(--apple-blue) inset; background: #ffffff; }

@media (max-width: 768px) {
  .home-page { padding: 16px; } .calendar-card { padding: 16px; }
  .page-header { flex-direction: column; align-items: flex-start; gap: 16px; }
  .month-selector { flex-wrap: wrap; } .calendar-day { min-height: 80px; padding: 6px; }
  .day-number { font-size: 13px; } .duty-pill { padding: 2px 4px; font-size: 10px; }
}
</style>