<!--
==============================值班配置管理======================================
实现功能：
  1、设置工作日排班
  2、设置周六大小周轮换排班
  3、设置月末冲刺排班
-->

<template>
  <el-dialog v-model="visible" title="值班配置管理" width="1000px" :close-on-click-modal="false" class="apple-dialog">
    <el-tabs v-model="configTab" class="apple-tabs">
      <el-tab-pane label="工作日排班" name="weekday">
        <div class="config-section">
          <div class="section-header">
            <span class="section-desc">工作日固定值班人员，每周生效。</span>
            <el-button class="apple-btn primary" type="primary" :icon="Plus" @click="handleEmitAdd('weekday')">编辑人员</el-button>
          </div>
          <el-table :data="configData.weekdayDutyList" class="apple-table" style="width: 100%; margin-top: 16px;">
            <el-table-column prop="userName" label="姓名" width="120" />
            <el-table-column prop="dept" label="部门" width="150" />
            <el-table-column prop="phone" label="手机号" width="150" />
            <el-table-column label="操作" width="150" align="center">
              <template #default="{ row }">
                <el-button type="danger" link class="apple-text-btn danger" :icon="Delete" @click="handleRemove(row)">移除</el-button>
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
              <el-date-picker v-model="internalBaseDate" type="date" placeholder="选择基准日期" format="YYYY-MM-DD" value-format="YYYY-MM-DD" class="apple-date-picker" style="width: 200px; margin-right: 12px;" @change="onBaseDateChange" />
              <span class="date-desc">设定此日期所在周为「单周」</span>
            </div>
          </div>
          <el-row :gutter="24" style="margin-top: 24px;">
            <el-col :span="12">
              <div class="apple-sub-card">
                <div class="group-header"><h4>单周组</h4><el-button class="apple-btn small primary" type="primary" :icon="Plus" @click="handleEmitAdd('group1')">编辑</el-button></div>
                <el-table :data="configData.saturdayGroup1" class="apple-table small" style="width: 100%; margin-top: 16px;">
                  <el-table-column prop="userName" label="姓名" />
                  <el-table-column prop="dept" label="部门" />
                  <el-table-column label="操作" width="80" align="center">
                    <template #default="{ row }">
                      <el-button type="danger" link class="apple-text-btn danger" @click="handleRemove(row)">移除</el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="apple-sub-card">
                <div class="group-header"><h4>双周组</h4><el-button class="apple-btn small primary" type="primary" :icon="Plus" @click="handleEmitAdd('group2')">编辑</el-button></div>
                <el-table :data="configData.saturdayGroup2" class="apple-table small" style="width: 100%; margin-top: 16px;">
                  <el-table-column prop="userName" label="姓名" />
                  <el-table-column prop="dept" label="部门" />
                  <el-table-column label="操作" width="80" align="center">
                    <template #default="{ row }">
                      <el-button type="danger" link class="apple-text-btn danger" @click="handleRemove(row)">移除</el-button>
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
            <el-button class="apple-btn primary" type="primary" :icon="Plus" @click="handleEmitAdd('monthEnd')">编辑人员</el-button>
          </div>
          <el-table :data="configData.monthEndDutyList" class="apple-table" style="width: 100%; margin-top: 16px;">
            <el-table-column prop="userName" label="姓名" width="120" />
            <el-table-column prop="dept" label="部门" width="150" />
            <el-table-column prop="phone" label="手机号" width="150" />
            <el-table-column label="操作" width="150" align="center">
              <template #default="{ row }">
                <el-button type="danger" link class="apple-text-btn danger" :icon="Delete" @click="handleRemove(row)">移除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>
    </el-tabs>

    <template #footer>
      <div class="dialog-footer apple-footer">
        <el-button class="apple-btn" @click="visible = false">关闭</el-button>
        <el-button class="apple-btn primary" type="primary" @click="$emit('save-config')">保存并刷新</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { Plus, Delete } from '@element-plus/icons-vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { deleteDutyPerson } from '@/api/index'

const props = defineProps({ modelValue: Boolean, configData: Object, baseDate: String })
const emit = defineEmits(['update:modelValue', 'add-person', 'save-config', 'base-date-change', 'request-reload'])

const visible = computed({ get: () => props.modelValue, set: (val) => emit('update:modelValue', val) })
const configTab = ref('weekday')
const internalBaseDate = ref(props.baseDate)

watch(() => props.baseDate, (newVal) => internalBaseDate.value = newVal)

const handleEmitAdd = (type) => emit('add-person', type)
const onBaseDateChange = (val) => emit('base-date-change', val)

const handleRemove = (person) => {
  ElMessageBox.prompt('请输入移除原因', '移除确认', {
    confirmButtonText: '确定', cancelButtonText: '取消', inputPlaceholder: '请输入原因...',
    inputValidator: (v) => (!v || v.trim().length < 2) ? '至少需要2个字符' : true
  }).then(async ({ value: remark }) => {
    if (person.dutyPersonId) {
      const res = await deleteDutyPerson({ id: person.dutyPersonId, remark })
      if (res.code === 0) {
        ElMessage.success('移除成功')
        emit('request-reload') // 请求父组件刷新
      }
    }
  }).catch(() => {})
}
</script>

<style scoped>
.config-section { padding: 8px 0; }
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.section-desc { color: #86868b; font-size: 13px; }
.apple-sub-card { background: #fafafa; border: 1px solid rgba(0,0,0,0.06); border-radius: 14px; padding: 16px; margin-bottom: 20px; }
.group-header { display: flex; justify-content: space-between; align-items: center; }
.group-header h4 { margin: 0; font-weight: 600; font-size: 15px; }
.base-date-config { display: flex; align-items: center; background: #f5f5f7; padding: 12px 16px; border-radius: 12px; width: 100%; }
.base-date-config .label { font-weight: 600; margin-right: 12px; font-size: 14px; }
.date-desc { font-size: 12px; color: #86868b; margin-left: 12px; }
.apple-text-btn.danger { color: #ff3b30; }
</style>