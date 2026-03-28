package com.zjintu.schedul.model.entity.dupt;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 值班配置表
 */
@TableName("duty_config")
@Data
public class DutyConfig implements Serializable {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 基准日期（用于判断单周/双周，该日期所在周为单周）
     */
    private Date baseDate;

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

