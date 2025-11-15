package com.zjintu.schedul.model.dto.duty;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 根据日期查询值班人员请求
 */
@Data
public class DutyPersonByDateRequest implements Serializable {
    /**
     * 查询日期
     */
    private Date date;

    private static final long serialVersionUID = 1L;
}

