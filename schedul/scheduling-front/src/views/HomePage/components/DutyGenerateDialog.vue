<!--
 =============================值班生成记录部分
 实现功能：
    前辈写的还不知道是干嘛用的
-->

<template>
  <el-dialog v-model="visible" title="生成值班记录" width="460px" class="apple-dialog" :close-on-click-modal="false">
    <div class="apple-alert">
      <el-icon><Calendar /></el-icon>
      <div class="alert-content">
        <p>系统每天凌晨 02:00 会自动生成当天的记录。</p>
        <p>仅需补全历史或预生成记录时使用此功能。</p>
      </div>
    </div>
    <el-form :model="form" label-position="top" class="apple-form" style="margin-top:20px;">
      <el-form-item label="开始日期">
        <el-date-picker v-model="form.startDate" type="date" placeholder="选择开始日期" format="YYYY-MM-DD" value-format="YYYY-MM-DD" class="apple-date-picker" style="width: 100%" />
      </el-form-item>
      <el-form-item label="结束日期">
        <el-date-picker v-model="form.endDate" type="date" placeholder="选择结束日期" format="YYYY-MM-DD" value-format="YYYY-MM-DD" class="apple-date-picker" style="width: 100%" />
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer apple-footer">
        <el-button class="apple-btn" @click="visible = false">取消</el-button>
        <el-button class="apple-btn primary" type="primary" @click="handleGenerate" :loading="submitting">立即生成</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed } from 'vue';
import { ElMessage } from 'element-plus';
import { Calendar } from '@element-plus/icons-vue';
import { generateDutyRecords } from '@/api/index';

const props = defineProps({ modelValue: Boolean });
const emit = defineEmits(['update:modelValue', 'success']);
const visible = computed({ get: () => props.modelValue, set: (val) => emit('update:modelValue', val) });

const submitting = ref(false);
const form = reactive({ startDate: '', endDate: '' });

const handleGenerate = async () => {
  if (!form.startDate || !form.endDate) return ElMessage.warning('请选择起止日期');
  if (new Date(form.startDate) > new Date(form.endDate)) return ElMessage.warning('开始不能晚于结束日期');
  submitting.value = true;
  try {
    const res = await generateDutyRecords(form);
    if (res.code === 0) {
      ElMessage.success(`成功生成 ${res.data} 条记录`);
      emit('success');
      visible.value = false;
      form.startDate = '';
      form.endDate = '';
    }
  } finally { submitting.value = false; }
};
</script>

<style scoped>
.apple-alert { display: flex; align-items: flex-start; background: #e5f0ff; border-radius: 8px; padding: 16px; color: #007aff; }
.apple-alert .el-icon { font-size: 20px; margin-right: 12px; margin-top: 2px; }
.alert-content p { margin: 0 0 4px 0; font-size: 13px; line-height: 1.5; }
</style>