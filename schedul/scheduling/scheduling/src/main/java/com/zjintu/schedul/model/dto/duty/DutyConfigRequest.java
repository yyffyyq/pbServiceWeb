package com.zjintu.schedul.model.dto.duty;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 值班配置请求
 */
@Data
public class DutyConfigRequest implements Serializable {
    /**
     * 基准日期
     */
    private Date baseDate;

    private static final long serialVersionUID = 1L;
}

