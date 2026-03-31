import request from '@/utils/request';

// 1. 获取部门列表
export function getDeptList() {
    return request({
        url: '/duty/getDept',
        method: 'get'
    });
}

// 2. 根据部门ID获取用户列表 (分页参数通常在 params 里传)
export function getUserByDeptId(deptId, params) {
    return request({
        url: `/user/getUserBydeptId/${deptId}`,
        method: 'get',
        params // { current, pageSize, userName... }
    });
}

// 3. 批量新增临时分组人员
export function addTempDutyBatch(data) {
    return request({
        url: '/duty/temperate/addBatch',
        method: 'post',
        data // { baseDate, dutyType, remark, userIdList }
    });
}
// 获取带有当天临时组值班状态的部门人员列表
export function getTempDutyAccountByDeptId(deptId, params) {
    return request({
        url: `/user/temporary/account/${deptId}`,
        method: 'get',
        params // 包含 dutyDate, current, pageSize
    });
}