package com.zjintu.schedul.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 值班配置VO
 */
@Data
public class DutyConfigVO implements Serializable {
    /**
     * 基准日期
     */
    private Date baseDate;

    /**
     * 工作日值班人员列表
     */
    private List<DutyPersonVO> weekdayDutyList;

    /**
     * 周六单周组
     */
    private List<DutyPersonVO> saturdayGroup1;

    /**
     * 周六双周组
     */
    private List<DutyPersonVO> saturdayGroup2;

    /**
     * 月末值班人员列表
     */
    private List<DutyPersonVO> monthEndDutyList;

    private static final long serialVersionUID = 1L;
}

