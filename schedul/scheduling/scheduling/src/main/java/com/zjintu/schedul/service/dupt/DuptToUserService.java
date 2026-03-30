package com.zjintu.schedul.service.dupt;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zjintu.schedul.model.entity.dupt.DeptToUser;
import com.zjintu.schedul.model.vo.userVO.UserVO;

import java.util.List;

/**
 * 关联用户user与dept通过id
 */
public interface DuptToUserService extends IService<DeptToUser> {

    /**
     * 获取用户id通过deptId
     * @param deptId
     * @return
     */
    List<UserVO> getdutyVOlist(Long deptId);
}
