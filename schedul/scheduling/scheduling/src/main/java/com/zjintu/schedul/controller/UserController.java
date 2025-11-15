package com.zjintu.schedul.controller;

import com.zjintu.schedul.common.BaseResponse;
import com.zjintu.schedul.common.ErrorCode;
import com.zjintu.schedul.common.ResultUtils;
import com.zjintu.schedul.exception.ThrowUtils;
import com.zjintu.schedul.model.dto.user.UserLoginRequest;
import com.zjintu.schedul.model.dto.user.UserRegisterRequest;
import com.zjintu.schedul.model.entity.User;
import com.zjintu.schedul.model.vo.LoginUserVO;
import com.zjintu.schedul.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.security.interfaces.RSAMultiPrimePrivateCrtKey;

@RestController
@RequestMapping("/user")

public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户注册
     *
     * @param request
     * @return
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        String userAccount = request.getUserAccount();
        String userPassword = request.getUserPassword();
        String checkPassword = request.getCheckPassword();
        String userName = request.getUserName();
        String phone = request.getPhone();
        String dept = request.getDept();
        long result = userService.userRegister(userAccount, userPassword, checkPassword, userName, phone, dept);

        return ResultUtils.success(result);
    }
    /**
     * 用户登录
     *
     * @param userLoginRequest
     * @param request
     * @return
     */
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest,
                                               HttpServletRequest request) {
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, request);

        return ResultUtils.success(loginUserVO);
    }

    /**
     * 获取当前登录用户
     */
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(userService.getLoginUserVO(loginUser));
    }
    /**
     * 用户退出登录
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLoginOut(HttpServletRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        boolean result = userService.userLoginOut(request);

        return ResultUtils.success(result);
    }

}
