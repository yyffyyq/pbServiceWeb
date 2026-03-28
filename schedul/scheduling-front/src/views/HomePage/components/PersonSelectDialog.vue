<!--
  ========================值班人员添加部分=============================
  功能实现：
    1、实现值班人员的添加功能
-->


<template>
  <el-dialog v-model="visible" :title="title" width="540px" class="apple-dialog" @close="handleClose">
    <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="apple-form">
      <el-form-item label="选择人员" prop="userIds">
        <el-cascader
            v-model="form.userIds" :options="cascaderOptions" :props="cascaderProps"
            placeholder="搜索或选择部门及人员（可多选）" clearable filterable collapse-tags collapse-tags-tooltip
            class="apple-cascader" style="width: 100%;"
        >
          <template #default="{ data }">
            <span>{{ data.label }}</span>
            <span v-if="data.account" class="cascader-account">({{ data.account }})</span>
          </template>
        </el-cascader>
      </el-form-item>
      <el-form-item label="已选择" v-if="form.userIds && form.userIds.length > 0">
        <div class="apple-selected-users">
          <div class="apple-user-tag" v-for="userId in form.userIds" :key="userId">
            {{ getUserNameById(userId) }}
            <span class="tag-close" @click="removeSelectedUser(userId)">×</span>
          </div>
        </div>
      </el-form-item>
      <el-form-item label="调班备注" prop="remark">
        <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入调班原因或备注信息..." maxlength="200" show-word-limit class="apple-textarea" />
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer apple-footer">
        <el-button class="apple-btn" @click="visible = false">取消</el-button>
        <el-button class="apple-btn primary" type="primary" @click="handleConfirm" :loading="submitting">完成</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue';
import { getDept, getUserBydeptId } from '@/api/index';

const props = defineProps({ modelValue: Boolean, title: String, initialUserIds: Array, editMode: Boolean });
const emit = defineEmits(['update:modelValue', 'submit-success']);

const visible = computed({ get: () => props.modelValue, set: (val) => emit('update:modelValue', val) });
const formRef = ref(null);
const submitting = ref(false);
const deptWithUsersList = ref([]);
const allUsers = ref([]);

const form = reactive({ userIds: [], remark: '' });
const rules = { userIds: [{ required: true, message: '请选择用户', type: 'array', trigger: 'change' }] };
const cascaderProps = { multiple: true, emitPath: false, value: 'value', label: 'label' };

const cascaderOptions = computed(() => {
  if (!deptWithUsersList.value) return [];
  const options = deptWithUsersList.value.map(group => ({
    value: `dept_${group.dept.id}`, label: group.dept.deptName,
    children: group.users.length > 0 ? group.users.map(user => ({ value: user.id, label: user.userName, account: user.userAccount })) : undefined
  }));
  const assignedIds = deptWithUsersList.value.flatMap(g => g.users.map(u => u.id));
  const unassigned = allUsers.value.filter(u => !assignedIds.includes(u.id));
  if (unassigned.length > 0) {
    options.push({ value: 'dept_unassigned', label: '未设置部门', children: unassigned.map(user => ({ value: user.id, label: user.userName, account: user.userAccount })) });
  }
  return options;
});

const loadData = async () => {
  if (deptWithUsersList.value.length > 0) return;
  try {
    const deptRes = await getDept();
    if (deptRes.code === 0 && deptRes.data) {
      const enrichedUsers = [];
      const promises = deptRes.data.map(async (dept) => {
        const userRes = await getUserBydeptId(dept.id, { current: 1, pageSize: 1000 });
        const users = (userRes.code === 0 && userRes.data?.records) ? userRes.data.records : [];
        users.forEach(u => { u.dept = dept.deptName; enrichedUsers.push(u); });
        return { dept, users };
      });
      deptWithUsersList.value = await Promise.all(promises);
      allUsers.value = enrichedUsers;
    }
  } catch (e) {}
};

const getUserNameById = (id) => allUsers.value.find(u => u.id === id)?.userName || '未知';
const removeSelectedUser = (id) => { form.userIds = form.userIds.filter(i => i !== id); };

const handleClose = () => { form.userIds = []; form.remark = ''; formRef.value?.clearValidate(); };

const handleConfirm = async () => {
  await formRef.value.validate((valid) => {
    if (!valid) return;
    submitting.value = true;
    const selectedUsers = form.userIds.map(id => allUsers.value.find(u => u.id === id)).filter(Boolean);
    emit('submit-success', { userIds: form.userIds, selectedUsers, remark: form.remark });
    submitting.value = false;
    visible.value = false;
  });
};

watch(() => props.modelValue, (val) => {
  if (val) {
    loadData();
    form.userIds = [...(props.initialUserIds || [])];
  }
});
</script>

<style scoped>
.cascader-account { font-size: 12px; color: #86868b; margin-left: 8px; }
.apple-selected-users { display: flex; flex-wrap: wrap; gap: 8px; padding: 12px; background: #f5f5f7; border-radius: 12px; min-height: 44px; }
.apple-user-tag { background: #fff; border: 1px solid rgba(0,0,0,0.08); border-radius: 20px; padding: 4px 12px; font-size: 13px; font-weight: 500; display: flex; align-items: center; box-shadow: 0 2px 4px rgba(0,0,0,0.02); }
.tag-close { margin-left: 6px; cursor: pointer; color: #86868b; font-size: 16px; line-height: 1; }
.tag-close:hover { color: #ff3b30; }
</style>