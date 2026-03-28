package com.zjintu.schedul.model.dto.duty;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;

@Data
public class DutyHolidayRequest implements Serializable {
    /**
     * 添加休息日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private String holidayDate;
}
