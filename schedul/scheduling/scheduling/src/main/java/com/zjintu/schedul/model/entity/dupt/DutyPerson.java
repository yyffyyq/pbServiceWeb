package com.zjintu.schedul.model.entity.dupt;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 值班人员表
 */
@TableName("duty_person")
@Data
public class DutyPerson implements Serializable {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户ID（关联sys_user表）
     */
    private Long userId;

    /**
     * 值班类型：weekday-工作日值班, saturday_group1-周六单周组, saturday_group2-周六双周组, month_end-月末值班
     */
    private String dutyType;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    private static final long serialVersionUID = 1L;
}

