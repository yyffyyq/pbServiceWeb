package com.zjintu.schedul.model.dto.duty;

import lombok.Data;

import java.io.Serializable;

/**
 * 统计值班天数请求
 */
@Data
public class DutyCountRequest implements Serializable {
    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 统计类型：month-本月, year-本年
     */
    private String type;

    private static final long serialVersionUID = 1L;
}

