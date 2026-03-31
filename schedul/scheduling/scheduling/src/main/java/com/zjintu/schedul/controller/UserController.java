package com.zjintu.schedul.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.pinyin.PinyinUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zjintu.schedul.annotation.AuthCheck;
import com.zjintu.schedul.common.BaseResponse;
import com.zjintu.schedul.common.DeleteRequest;
import com.zjintu.schedul.common.ErrorCode;
import com.zjintu.schedul.common.ResultUtils;
import com.zjintu.schedul.constant.UserConstant;
import com.zjintu.schedul.exception.BusinessException;
import com.zjintu.schedul.exception.ThrowUtils;
import com.zjintu.schedul.model.dto.duty.DutyRecordTempareRequest;
import com.zjintu.schedul.model.dto.user.*;
import com.zjintu.schedul.model.entity.user.User;
import com.zjintu.schedul.model.entity.dupt.DeptToUser;
import com.zjintu.schedul.model.vo.duptVO.DutyRecordTempareVO;
import com.zjintu.schedul.model.vo.userVO.LoginUserVO;
import com.zjintu.schedul.model.vo.userVO.UserVO;
import com.zjintu.schedul.service.dupt.DutyRecordService;
import com.zjintu.schedul.service.dupt.DutyService;
import com.zjintu.schedul.service.user.UserService;
import com.zjintu.schedul.service.dupt.DuptToUserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user")

public class UserController {

    @Resource
    private UserService userService;


    @Resource
    private DutyService dutyService;

    @Resource
    private DuptToUserService duptToUserService;

    @Resource
    private DutyRecordService dutyRecordService;


    // 在类中注入或静态调用
    private static final Snowflake snowflake = IdUtil.getSnowflake(1, 1);

    /**
     * 临时添加组数据回显功能
     */
    @GetMapping("/temporary/account/{deptId}")
    @Operation(summary = "根据部门编号查询" ,description = "")
    public BaseResponse<Page<DutyRecordTempareVO>> selectTempareAccount(@PathVariable Long deptId,
            DutyRecordTempareRequest dutyRecordTempareRequest){
        ThrowUtils.throwIf(deptId==null,ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(dutyRecordTempareRequest.getDutyDate()==null,ErrorCode.PARAMS_ERROR,"日期参数不可为空");

        //通过部门id先获取到用户信息，然后再通过用户信息和日期获取到值班分组情况
        List<DutyRecordTempareVO> temporaryVOList = dutyRecordService.selectUserDuptTypeBydeptIdAndDuptDate(deptId,dutyRecordTempareRequest);
        // 获取封装后列表长度
        int total = temporaryVOList.size();
        // 那到分页查询信息
        long current = dutyRecordTempareRequest.getCurrent();
        long size = dutyRecordTempareRequest.getPageSize();

        /// 计算截取范围
        //todo 为什么要这么做？
        int formIndex= (int)((current-1)*size);
        int toIndex = Math.min(formIndex+(int)size, total);

        /// 2. 如果该部门没用户，直接返回空分页对象
        if (CollectionUtils.isEmpty(temporaryVOList)) {
            return ResultUtils.success(new Page<>(current, size, 0));
        }
        // 创建分页查询
        Page<DutyRecordTempareVO> dutyRecordTempareVOPage = new Page<>(current, size,total);
        // 安全截取：防止 current 过大导致 fromIndex > total
        // todo 为什么？
        if (formIndex < total && formIndex >= 0) {
            dutyRecordTempareVOPage.setRecords(temporaryVOList.subList(formIndex, toIndex));
        }
        // 返回分页值
        return ResultUtils.success(dutyRecordTempareVOPage);
    }


    /**
     * 用户查询列表，根据部门id
     */
    @GetMapping("/getUserBydeptId/{deptId}")
    @Operation(summary = "用户查询列表根据部门id", description = "用于添加员工下拉框子下拉框")
    public BaseResponse<Page<UserVO>> getUserByDeptId(@PathVariable Long deptId,
                                                      UserQueryRequest userQueryRequest){
        /// 判断部门id值是否为空
        ThrowUtils.throwIf(deptId == null, ErrorCode.PARAMS_ERROR);
        /// 判断分页查询值是否为空
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);

        /// 先通过deptId获取到关联用户id
        /// 拿查到的用户id去一个一个获取到用户信息通过这个id
        List<UserVO> userVOList = duptToUserService.getdutyVOlist(deptId);
        /// 获取封装后列表长度
        int total = userVOList.size();

        /// 那到分页查询信息
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();

        /// 计算截取范围
        //todo 为什么要这么做？
        int formIndex= (int)((current-1)*size);
        int toIndex = Math.min(formIndex+(int)size, total);

        /// 2. 如果该部门没用户，直接返回空分页对象
        if (CollectionUtils.isEmpty(userVOList)) {
            return ResultUtils.success(new Page<>(current, size, 0));
        }

        // 创建分页查询
        Page<UserVO> userVopage = new Page<>(current, size,total);
        // 安全截取：防止 current 过大导致 fromIndex > total
        // todo 为什么？
        if (formIndex < total && formIndex >= 0) {
            userVopage.setRecords(userVOList.subList(formIndex, toIndex));
        }
        // 返回分页值
        return ResultUtils.success(userVopage);
    }
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

    /**
     * 添加用户
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest) {
        ThrowUtils.throwIf(userAddRequest == null, ErrorCode.PARAMS_ERROR);
        User user = new User();
        BeanUtil.copyProperties(userAddRequest, user);
        final String DEFAULT_PASSWORD = "123456789";
        String encryptPassword = userService.getEncryptPassword(DEFAULT_PASSWORD);
        user.setUserPassword(encryptPassword);
        boolean result = userService.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(user.getId());
    }

    /**
     * 批量添加用户
     */
    @PostMapping("/batchAdd")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "批量插入员工",description = "用于员工批量插入")
    public BaseResponse<Boolean> batchAddUser(@RequestBody List<UserAddBatchRequest> userAddBatchRequestList) {
        if (userAddBatchRequestList == null || userAddBatchRequestList.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        final String DEFAULT_PASSWORD = "123456789";
        String encryptPassword = userService.getEncryptPassword(DEFAULT_PASSWORD);
        Long deptId=null;
        List<User> userList = new java.util.ArrayList<>();
        for (UserAddBatchRequest userAddBatchRequest : userAddBatchRequestList) {
            if(deptId==null){
                deptId=userAddBatchRequest.getDeptId();
            }
            /// 拿到账号
            String UserAccount = userAddBatchRequest.getUserAccount();
            // 用户名称拼音化
            if (StrUtil.isBlank(UserAccount)) {
                String userName = userAddBatchRequest.getUserName();
                String pinyinPrefix = "user";
                if (StrUtil.isNotBlank(userName)) {
                    // PinyinUtil.getPinyin(字符串, 分隔符)
                    // 例如："刘文摘" -> "liu wenzhai"，这里分隔符传 "" 就会变成 "liuwenzhai"
                    // 对于原本就是英文的 "Su Ming"，会保持不变并去掉空格变成 "suming"
                    pinyinPrefix = PinyinUtil.getPinyin(userName, "")
                            .replace(" ", "") // 去除可能存在的空格
                            .toLowerCase();   // 统一转为小写字母
                }
                // 拼接成最终的账号：拼音 + 雪花ID (可以加个下划线增加可读性，不需要的话把 "_" 去掉即可)
                String rawId = snowflake.nextIdStr();
                String shortId = rawId.substring(rawId.length() - 10);
                UserAccount = pinyinPrefix + "_" + shortId;
                if (UserAccount.length() > 20) {
                    UserAccount = UserAccount.substring(0, 20);
                }
            }
            userAddBatchRequest.setUserAccount(UserAccount);
            /// 默认话设置
            User user = new User();
            BeanUtil.copyProperties(userAddBatchRequest, user);
            user.setUserPassword(encryptPassword);
            userList.add(user);
        }
        // todo 批量插入用户之后拿到用户id，之后将这个值和部门id一起加入dept_to_user
        boolean result = userService.saveBatch(userList);
        if(result){
            /// 提取用户id列表
            List<Long> userIdList = userList.stream().map(User::getId).collect(Collectors.toList());
            Long finalDeptId = deptId;
            List<DeptToUser> deptToUserList = userIdList.stream().map(userId -> {
                DeptToUser deptToUser = new DeptToUser();
                deptToUser.setUserId(userId);
                deptToUser.setDeptId(finalDeptId);
                return deptToUser;
            }).collect(Collectors.toList());
            // 这里有问题
            duptToUserService.saveBatch(deptToUserList);

        }
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取用户（仅管理员）
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(user);
    }

    /**
     * 根据 id 获取包装类
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(long id) {
        BaseResponse<User> response = getUserById(id);
        User user = response.getData();
        return ResultUtils.success(userService.getUserVO(user));
    }

    /**
     * 删除用户
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);
    }

    /**
     * 更新用户
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 获取用户列表（仅管理员）
     */
    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        Page<User> userPage = userService.page(new Page<>(current, size),
                userService.getQueryWrapper(userQueryRequest));
        Page<UserVO> userVOPage = new Page<>(current, size, userPage.getTotal());
        List<UserVO> userVOList = userService.getUserVOList(userPage.getRecords());
        userVOPage.setRecords(userVOList);
        return ResultUtils.success(userVOPage);
    }
}
