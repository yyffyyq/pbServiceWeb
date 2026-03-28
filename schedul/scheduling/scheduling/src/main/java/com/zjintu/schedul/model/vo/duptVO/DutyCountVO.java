package com.zjintu.schedul.model.vo.duptVO;

import lombok.Data;

import java.io.Serializable;

/**
 * 值班天数统计VO
 */
@Data
public class DutyCountVO implements Serializable {
    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 本月值班天数
     */
    private Integer monthCount;

    /**
     * 本年值班天数
     */
    private Integer yearCount;

    private static final long serialVersionUID = 1L;
}

