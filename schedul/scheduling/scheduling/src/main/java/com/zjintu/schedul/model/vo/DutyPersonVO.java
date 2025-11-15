package com.zjintu.schedul.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 值班人员VO
 */
@Data
public class DutyPersonVO implements Serializable {
    /**
     * duty_person表的ID（用于删除操作）
     */
    private Long dutyPersonId;

    /**
     * 用户ID
     */
    private Long id;

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

    /**
     * 值班类型
     */
    private String dutyType;

    private static final long serialVersionUID = 1L;
}

