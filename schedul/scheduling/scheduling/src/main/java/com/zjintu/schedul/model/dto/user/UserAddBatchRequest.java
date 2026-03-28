package com.zjintu.schedul.model.dto.user;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

@Data
public class UserAddBatchRequest {
    private String userAccount;
    private String userName;
    private String phone;
    /**
     * 部门id
     */
    private  Long deptId;
    private String dept;
    private String userRole;
}
