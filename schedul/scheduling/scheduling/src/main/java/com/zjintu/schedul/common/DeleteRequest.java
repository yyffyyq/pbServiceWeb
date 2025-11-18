package com.zjintu.schedul.common;

import java.io.Serializable;
import lombok.Data;

/**
 * 删除请求
 *
 
 */
@Data
public class DeleteRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 备注（用于记录删除原因）
     */
    private String remark;

    private static final long serialVersionUID = 1L;
}