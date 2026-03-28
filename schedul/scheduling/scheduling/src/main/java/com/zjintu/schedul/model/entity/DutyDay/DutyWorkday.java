package com.zjintu.schedul.model.entity.DutyDay;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.sql.Date;
@TableName("duty_workday")
@Data
public class DutyWorkday implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("work_date")
    private String workDate;

    @TableField("create_time")
    private Date createTime;

    @TableField("update_time")
    private Date updateTime;

    @TableLogic
    @TableField("is_delete")
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}