package com.zjintu.schedul.model.dto.duty;

import lombok.Data;

import java.io.Serializable;

/**
 * 添加值班人员请求
 */
@Data
public class DutyPersonAddRequest implements Serializable {
    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 值班类型：weekday-工作日值班, saturday_group1-周六单周组, saturday_group2-周六双周组, month_end-月末值班
     */
    private String dutyType;

    private static final long serialVersionUID = 1L;
}

