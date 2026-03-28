package com.zjintu.schedul.model.entity.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
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
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String dept;

    private String userAccount;

    private String userName;

    private String userPassword;

    private String phone;

    private String userRole;

    private Date createTime;

    private Date updateTime;
    @TableLogic
    private Integer isDelete;



    private static final long serialVersionUID = 1L;
}