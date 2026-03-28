package com.zjintu.schedul.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zjintu.schedul.common.ErrorCode;
import com.zjintu.schedul.exception.ThrowUtils;
import com.zjintu.schedul.mapper.DeptToUserMapper;
import com.zjintu.schedul.mapper.UserMapper;
import com.zjintu.schedul.model.entity.user.User;
import com.zjintu.schedul.model.entity.dupt.DeptToUser;
import com.zjintu.schedul.model.vo.userVO.UserVO;
import com.zjintu.schedul.service.dept.DeptToUserService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DeptToUserServiceImpl extends ServiceImpl<DeptToUserMapper, DeptToUser> implements DeptToUserService {
    @Resource
    private DeptToUserMapper deptToUserMapper;

    @Resource
    private UserMapper userMapper;
    /**
     * 获取用户id通过deptId
     * @param deptId 部门id
     * @return
     */
    @Override
    public List<UserVO> getdutyVOlist(Long deptId) {
        // 校验参数
        ThrowUtils.throwIf(deptId == null, ErrorCode.PARAMS_ERROR);
        // 调用查看userId列表
        QueryWrapper<DeptToUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("user_id");
        queryWrapper.eq("dept_id", deptId);
        List<Long> userIdList = deptToUserMapper.selectObjs(queryWrapper);
        if (userIdList.isEmpty()) {
            return new ArrayList<>();
        }
        /// 查询所有用户列表
        List<User> userList = userMapper.selectBatchIds(userIdList);

        // 4. 封装 User 为 UserVO
        /// todo 这是一个lamda表达式，记录学习一下
        return userList.stream().map(user -> {
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            return userVO;
            /// 这一步是将所有零散的数据放到一个list返回回去
        }).collect(Collectors.toList());
    }
}
