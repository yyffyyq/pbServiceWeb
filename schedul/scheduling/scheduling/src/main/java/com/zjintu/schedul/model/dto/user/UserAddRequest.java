package com.zjintu.schedul.model.dto.user;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserAddRequest implements Serializable {
    private String userAccount;
    private String userName;
    private String phone;
    private String dept;
    private String userRole;

}
