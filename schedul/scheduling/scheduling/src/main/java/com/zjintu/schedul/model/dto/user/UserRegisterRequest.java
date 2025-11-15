package com.zjintu.schedul.model.dto.user;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserRegisterRequest implements Serializable {
    private String userAccount;
    private String userPassword;
    private String checkPassword;
    private String userName;
    private String phone;
    private String dept;

}
