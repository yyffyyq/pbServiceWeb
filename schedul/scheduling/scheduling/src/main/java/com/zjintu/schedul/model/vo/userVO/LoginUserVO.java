package com.zjintu.schedul.model.vo.userVO;

import lombok.Data;

import java.util.Date;

/**
 * 用户登录后显示的用户信息（脱敏后）
 */
@Data
public class LoginUserVO {

    private Long id;

    private String userAccount;

    private String userName;

    private String userRole;

    private Date createTime;


    private Date updateTime;

}