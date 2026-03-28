package com.zjintu.schedul.model.dto.duty;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * 从指定日期开始更新值班配置请求
 */
@Data
public class DutyConfigUpdateFromRequest implements Serializable {
    /**
     * 起始日期（YYYY-MM-DD格式）
     */
    private String startDate;

    /**
     * 要更新的配置，包含以下可选字段：
     * - weekdayDutyList: 工作日值班人员列表
     * - saturdayGroup1: 周六单周组人员列表
     * - saturdayGroup2: 周六双周组人员列表
     * - monthEndDutyList: 月末值班人员列表
     * - clearedDates: 免排班顺延日期列表
     */
    private Map<String, Object> config;

    /**
     * 调班备注（更新配置的原因）
     */
    private String remark;

    private static final long serialVersionUID = 1L;
}
