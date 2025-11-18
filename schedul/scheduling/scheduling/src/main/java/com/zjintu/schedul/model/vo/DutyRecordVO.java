package com.zjintu.schedul.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 值班记录VO
 */
@Data
public class DutyRecordVO implements Serializable {
    /**
     * 记录ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 值班日期
     */
    private Date dutyDate;

    /**
     * 值班类型
     */
    private String dutyType;

    /**
     * 状态
     */
    private String status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 部门
     */
    private String dept;

    /**
     * 手机号
     */
    private String phone;

    private static final long serialVersionUID = 1L;
}

