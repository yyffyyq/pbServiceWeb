package com.zjintu.schedul.model.entity.dupt;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * @TableName duty_record
 */
@TableName(value ="duty_record")
@Data
public class DutyRecord implements Serializable {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;

    private Date dutyDate;

    private String dutyType;

    private String status;

    private String remark;

    private Date createTime;

    private Date updateTime;
    @TableLogic
    private Integer isDelete;

    private static final long serialVersionUID = 1L;
}