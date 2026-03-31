package com.zjintu.schedul.mapper.duty;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zjintu.schedul.model.entity.dupt.DeptToUser;

import java.util.List;

public interface DeptToUserMapper extends BaseMapper<DeptToUser> {
    /**
     * 通过部门id获取用户id列表
     * @param deptId 部门id
     * @return
     */
    List<Long> selectUserIdByDuptId(Long deptId);
}
