<!--
=================================单日用户排班编辑=======================
实现功能有：
  1、编辑排班
  2、排班表pdf导出
  3、增加设置为休息日按钮实现单日修改效果实现


-->
<template>
  <el-dialog v-model="visible" title="值班人员详情" width="900px" class="apple-dialog detail-dialog">
    <div class="detail-actions">
      <el-button v-if="isAdmin && info.isEditable" class="apple-btn primary-light" :icon="Edit" @click="$emit('edit-click')">编辑排班</el-button>
      <el-button v-if="isAdmin && info.isHoliday" class="apple-btn danger" @click="$emit('clear-holiday', info.dateStr)">
        清空放假
      </el-button>
      <el-button v-if="isAdmin && !info.isHoliday" class="apple-btn warning" @click="$emit('set-holiday', info.dateStr)">
        设为放假
      </el-button>
      <div v-else></div>
      <el-button class="apple-btn success" :icon="Download" @click="handleExport" :loading="exportLoading">导出 PNG</el-button>
    </div>
    <div ref="exportContainer" class="export-container apple-export-wrapper">
      <div class="duty-table-wrapper">
        <h2 class="apple-export-title">锦途周末值班表</h2>
        <table class="duty-export-table">
          <thead>
          <tr><th class="dept-header">部门</th><th class="date-header">{{ formatDateHeader(info.dateStr, info.isSaturday) }}</th></tr>
          </thead>
          <tbody>
          <template v-if="info.dutyPersons && info.dutyPersons.length > 0">
            <template v-for="(deptGroup, deptName) in groupByDept(info.dutyPersons)" :key="deptName">
              <tr v-for="(person, index) in deptGroup" :key="person.userName">
                <td v-if="index === 0" :rowspan="deptGroup.length" class="dept-cell">{{ deptName }}</td>
                <td class="person-cell">
                  <div class="person-info-cell"><span class="person-name-text">{{ person.userName }}</span><span class="person-phone-text">{{ person.phone || '未设置' }}</span></div>
                </td>
              </tr>
            </template>
          </template>
          <tr v-else><td colspan="2" class="empty-message">该日期暂无人员排班</td></tr>
          </tbody>
        </table>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, computed, nextTick } from 'vue';
import { ElMessage } from 'element-plus';
import { Edit, Download } from '@element-plus/icons-vue';
import html2canvas from 'html2canvas';

const props = defineProps({ modelValue: Boolean, info: Object, isAdmin: Boolean });
// 设置按键触发事件
const emit = defineEmits(['update:modelValue', 'edit-click','set-holiday', 'clear-holiday']);
const visible = computed({ get: () => props.modelValue, set: (val) => emit('update:modelValue', val) });
const exportContainer = ref(null);
const exportLoading = ref(false);

const groupByDept = (persons) => { const groups = {}; persons.forEach(p => { const d = p.dept || '未设置'; if (!groups[d]) groups[d] = []; groups[d].push(p) }); return groups; };
const formatDateHeader = (dateStr, isSaturday) => { if (!dateStr) return ''; const d = new Date(dateStr); return `${isSaturday ? '周六' : '周日'}\n（${d.getMonth() + 1}月${d.getDate()}日）`; };

const handleExport = async () => {
  exportLoading.value = true;
  try {
    await nextTick();
    const container = exportContainer.value;
    const targetNode = container.querySelector('.duty-table-wrapper') || container;
    const rect = targetNode.getBoundingClientRect();
    const canvas = await html2canvas(targetNode, { scale: 2, useCORS: true, backgroundColor: '#ffffff', width: targetNode.scrollWidth, height: Math.max(rect.height, targetNode.scrollHeight) + 100 });
    const blob = await new Promise(r => canvas.toBlob(r, 'image/png'));
    const url = URL.createObjectURL(blob); const a = document.createElement('a'); a.download = `排班_${props.info.dateStr}.png`; a.href = url; a.click(); URL.revokeObjectURL(url);
    ElMessage.success('导出成功');
  } catch (e) {} finally { exportLoading.value = false; }
};
</script>

<style scoped>
.detail-actions { display: flex; justify-content: space-between; margin-bottom: 24px; }
.apple-export-wrapper { padding: 40px; background: #fff; border-radius: 0; }
.duty-table-wrapper { background: #fff; padding: 20px; }
.apple-export-title { text-align: center; font-size: 28px; font-weight: 700; margin-bottom: 32px; color: #1d1d1f; }
.duty-export-table { width: 100%; border-collapse: collapse; border: 2px solid #1d1d1f; }
.duty-export-table th, .duty-export-table td { border: 1px solid #1d1d1f; padding: 12px; text-align: center; vertical-align: middle; }
.dept-header { width: 30%; background: #f5f5f7; font-size: 18px; }
.date-header { white-space: pre-line; font-size: 18px; }
.dept-cell { font-size: 18px; font-weight: 700; background: #f5f5f7; }
.person-info-cell { display: flex; flex-direction: column; gap: 4px; }
.person-name-text { font-size: 20px; font-weight: 600; }
.person-phone-text { font-size: 14px; color: #424245; }
.empty-message { padding: 40px; color: #86868b; font-style: italic; }
</style>