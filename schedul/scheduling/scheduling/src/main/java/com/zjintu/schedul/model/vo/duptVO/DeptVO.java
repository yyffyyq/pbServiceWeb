package com.zjintu.schedul.model.vo.duptVO;

import lombok.Data;

import java.io.Serializable;

@Data
public class DeptVO implements Serializable {
    /**
     * 部门id
     */
    private Long id;
    /**
     * 部门名称方便后续显示
     */
    private  String deptName;
}
