package com.zjintu.schedul.model.dto.duty;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 新增临时值班人员分组请求
 */
@Data
public class DutyPersonTempareAddRequest implements Serializable {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 值班类型：weekday-工作日值班, saturday_group1-周六单周组, saturday_group2-周六双周组, month_end-月末值班, groupe_temporaire-临时分组
     */
    private String dutyType;

    /**
     * 调班备注（添加或删除人员的原因）
     */
    private String remark;

    /**
     * 新增临时组日期
     */
    private Date baseDate;
    private static final long serialVersionUID = 1L;


}
