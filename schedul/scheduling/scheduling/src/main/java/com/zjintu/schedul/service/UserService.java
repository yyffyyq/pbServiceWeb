package com.zjintu.schedul.service;

import com.zjintu.schedul.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zjintu.schedul.model.vo.LoginUserVO;

import javax.servlet.http.HttpServletRequest;

/**
* @author Administrator
* @description 针对表【sys_user(系统用户表)】的数据库操作Service
* @createDate 2025-11-14 13:40:43
*/
public interface UserService extends IService<User> {

    User getLoginUser(HttpServletRequest request);

    long userRegister(String userAccount, String userPassword, String checkPassword,String name, String phone, String dept);

    String getEncryptPassword(String userPassword);

    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    LoginUserVO getLoginUserVO(User user);

    boolean userLoginOut(HttpServletRequest request);
}
