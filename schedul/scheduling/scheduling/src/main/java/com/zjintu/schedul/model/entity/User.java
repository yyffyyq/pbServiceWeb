package com.zjintu.schedul.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * @TableName sys_user
 */
@TableName(value ="sys_user")
@Data
public class User implements Serializable {
    private Long id;

    private String dept;

    private String userAccount;

    private String userName;

    private String userPassword;

    private String phone;

    private String userRole;

    private Date createtime;

    private Date updatetime;

    private Integer isdelete;



    private static final long serialVersionUID = 1L;
}