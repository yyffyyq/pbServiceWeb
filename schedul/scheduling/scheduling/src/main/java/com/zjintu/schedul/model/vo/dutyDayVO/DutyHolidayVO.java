package com.zjintu.schedul.model.vo.dutyDayVO;

import lombok.Data;

import java.io.Serializable;

@Data
public class DutyHolidayVO implements Serializable {
    private Long id;
    /**
     * 前端显示日期
     */
    private String holidayDate;
}
