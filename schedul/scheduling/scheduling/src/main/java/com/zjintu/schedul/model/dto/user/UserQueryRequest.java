package com.zjintu.schedul.model.dto.user;

import com.zjintu.schedul.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;


@Data
public class UserQueryRequest extends PageRequest implements Serializable {

    private Long id;

    private String userName;

    private String userAccount;

    private String userRole;

    private static final long serialVersionUID = 1L;
}