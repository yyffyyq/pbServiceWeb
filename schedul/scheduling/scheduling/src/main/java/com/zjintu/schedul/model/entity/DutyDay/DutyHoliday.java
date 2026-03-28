package com.zjintu.schedul.model.entity.DutyDay;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.sql.Date;

@TableName("duty_holiday")
@Data
public class DutyHoliday implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("holiday_date")
    private String holidayDate;
    @TableField("holiday_name")
    private String holidayName;

    /**
     * 类型: 0-法定节假日, 1-手动清空顺延
     */
    @TableField("type")
    private Integer type;
    @TableField("create_time")
    private Date createTime;

    @TableLogic
    @TableField("is_delete")
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
