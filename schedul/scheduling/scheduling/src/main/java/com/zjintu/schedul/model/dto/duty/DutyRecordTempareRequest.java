package com.zjintu.schedul.model.dto.duty;

import com.zjintu.schedul.common.PageRequest;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

@Data
public class DutyRecordTempareRequest extends PageRequest implements Serializable {
    /**
     * 日期
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date dutyDate;

    private static final long serialVersionUID = 1L;
}
