package com.zjintu.schedul.model.vo.duptVO;

import com.zjintu.schedul.common.PageRequest;
import lombok.Data;

import java.io.Serializable;

/**
 * 临时组数据回显
 */
@Data
public class DutyRecordTempareVO extends PageRequest implements Serializable {
    /**
     * 用户id
     */
    private Long UserId;
    /**
     * 用户姓名
     */
    private String userName;
    /**
     * 用户该日值班组
     */
    private String dutyType;
}
