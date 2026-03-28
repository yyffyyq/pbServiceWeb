package com.zjintu.schedul.model.entity.dupt;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@TableName("dept")
@Data
public class Duty implements Serializable {
    /**
     * 部门id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 部门名称方便后续显示
     */
    @TableField("dept_name")
    private  String deptName;
}
