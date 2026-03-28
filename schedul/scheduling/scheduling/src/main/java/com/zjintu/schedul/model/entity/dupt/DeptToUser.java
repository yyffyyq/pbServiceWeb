package com.zjintu.schedul.model.entity.dupt;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
@TableName("dept_to_user")
@Data
public class DeptToUser {
    /**
     * 部门与员工关联id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 部门id
     */
    @TableField("dept_id")
    private  Long deptId;
    /**
     * 员工id
     */
    @TableField("user_id")
    private  Long userId;
}
