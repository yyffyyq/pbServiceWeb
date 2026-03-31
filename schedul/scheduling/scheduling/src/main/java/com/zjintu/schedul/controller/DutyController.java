package com.zjintu.schedul.controller;

import com.zjintu.schedul.annotation.AuthCheck;
import com.zjintu.schedul.common.BaseResponse;
import com.zjintu.schedul.common.DeleteRequest;
import com.zjintu.schedul.common.ErrorCode;
import com.zjintu.schedul.common.ResultUtils;
import com.zjintu.schedul.constant.UserConstant;
import com.zjintu.schedul.exception.ThrowUtils;
import com.zjintu.schedul.model.dto.duty.*;
import com.zjintu.schedul.model.vo.duptVO.DeptVO;
import com.zjintu.schedul.model.vo.duptVO.DutyConfigVO;
import com.zjintu.schedul.model.vo.duptVO.DutyPersonVO;
import com.zjintu.schedul.service.dupt.DutyRecordService;
import com.zjintu.schedul.service.dupt.DutyService;
import com.zjintu.schedul.utils.ClearDateUtils;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 值班管理控制器
 */
@RestController
@RequestMapping("/duty")
public class DutyController {

    @Resource
    private DutyService dutyService;

    @Resource
    private DutyRecordService dutyRecordService;


    /***
     * 新增功能：
     * 获取部门信息接口
     */
    @GetMapping("/getDept")
    @Operation(summary = "获取部门信息", description = "用于查询部门信息列表")
    public BaseResponse<List<DeptVO>> getDeptList(){
        List<DeptVO> deptVOList = dutyService.selectDeptVOList();
        return ResultUtils.success(deptVOList);
    }


    /**
     * 获取值班配置（包括基准日期和所有值班人员列表）
     * 所有用户都可以访问
     */
    @GetMapping("/config")
    public BaseResponse<DutyConfigVO> getDutyConfig() {
        DutyConfigVO config = dutyService.getDutyConfig();
        return ResultUtils.success(config);
    }

    /**
     * 保存/更新基准日期（仅管理员）
     */
    @PostMapping("/config/base-date")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> saveBaseDate(@RequestBody DutyConfigRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        Boolean result = dutyService.saveBaseDate(request);
        return ResultUtils.success(result);
    }

    /**
     * 添加值班人员（仅管理员）
     */
    @PostMapping("/person/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addDutyPerson(@RequestBody DutyPersonAddRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        Long id = dutyService.addDutyPerson(request);
        // 添加人员后，更新从今天开始的未来值班记录
        try {
            dutyRecordService.updateFutureDutyRecords(request.getRemark());
        } catch (Exception e) {
            // 记录更新失败不影响添加操作
            e.printStackTrace();
        }
        return ResultUtils.success(id);
    }

/// ================================临时组相关业务逻辑======================================================
    /**
     * 添加临时值班人员(仅管理员) 这是单个插入
     * @param request 新增临时值班人员
     * @return
     */
    // todo 这里有逻辑上的问题，在上面单双分组修改之后会把这个里的isDelete的值修改成1也就是逻辑删除，我想要重新加入值会显示已经加入，但是逻辑上是删除了的，
    // todo 我现在需要把isDelete修改成0
    @PostMapping("/temperate/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "新增临时分组人员接口", description = "用于某日临时分组新增值班人员")
    public BaseResponse<String> addTemperateDutyPersoon(@RequestBody DutyPersonTempareAddRequest request){
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
//        Long id = dutyService.addTemperateDutyPerson(request);
        try{
            // 获取到需要修改的那天
            Date currentDate = request.getBaseDate();
            // 拿着日期去更新
            String result = dutyRecordService.updateTheDayDutyRecord(currentDate,request.getUserId(),request.getDutyType());
            return ResultUtils.success("添加成功"+result);
        }catch (Exception e){
            e.printStackTrace();
        }
        return ResultUtils.success("添加成功");
    }
    // 这里在单个插入的基础上进行批量插入临时组
    @PostMapping("/temperate/addBatch")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "新增临时分组批量人员接口", description = "用于某日临时分组新增值班批量人员")
    public BaseResponse<List<String>> addBatchTemperateDutyPerson(@RequestBody DutyPersonTempareAddBatchRequest requestList){
        ThrowUtils.throwIf(requestList == null, ErrorCode.PARAMS_ERROR);
        // todo我这里想要做一个接口二次判断，返回回来的对比这一天数据库的值isdelete为1的值，有哪一些是没有的，直接把他逻辑删除
        // 获取增加日期，处理日期格式
        Date currentDate = ClearDateUtils.atStartOfDay(requestList.getBaseDate());
        // 获取分组名称
        String type_group = requestList.getDutyType();
        // 先获取现在这一天的临时组用户ID
        List<Long> getUserTodayAllUserIdList = dutyRecordService.selectUserIdByGroupAndDate(currentDate,type_group);
        if(getUserTodayAllUserIdList==null){
            getUserTodayAllUserIdList = new ArrayList<>();
        }
        // 把请求里的用户列表拿出来和这个ReadyToDelete列表对比选出需要添加操作的，需要删除操作的，和不需要操作的，这里处理好数据让数据库压力小一点
        List<Long> readyToDeleteUserIdList = new ArrayList<>();
        List<Long> readyToAddUserIdList = new ArrayList<>();
        // 获取用户是否添加成功值
        List<Long> RequestUserIdList = requestList.getUserIdList();
        if(RequestUserIdList==null){
            RequestUserIdList = new ArrayList<>();
        }
        // 进行判断筛选
        // 采用hashset操作
        Set<Long> existUserIdSet = new HashSet<>(getUserTodayAllUserIdList);
        Set<Long> requesetUserIdSet = new HashSet<>(RequestUserIdList);

        // 需要删除的用户id列表
        Set<Long> readyToDeleteUserIdSet = new HashSet<>(existUserIdSet);
        readyToDeleteUserIdSet.remove(requesetUserIdSet);

        // 需要新增的用户id列表
        Set<Long> readyToAddUserIdSet = new HashSet<>(requesetUserIdSet);
        readyToAddUserIdSet.remove(existUserIdSet);

        List<String> resultList = new ArrayList<>();

        // todo这里做一个简单的对比选择出需要修改的用户id列表，然后进行操作，用户列表操作分为需要逻辑删除部分和需要添加部分，添加部分就使用下面这个
        try{
            // 删除多余用户操作
            for(Long deleteUserId :readyToDeleteUserIdSet){
                String result = dutyRecordService.deleteTheDayDutyRecord(currentDate,deleteUserId,type_group);
                resultList.add(result);
            }


            // 添加用户操作
            for(Long addUserId : readyToAddUserIdSet){
                // 获取需要修改的那天
                // 拿着日期去更新
                String result = dutyRecordService.updateTheDayDutyRecord(currentDate,addUserId,type_group);
                resultList.add(result);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return ResultUtils.success(resultList);
    }


    // 查看临时组情况

    // 删除临时组人员值班
    @PostMapping("/temperate/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "删除临时分组人员接口", description = "用于某日临时分组删除值班人员")
    public BaseResponse<String> deleteTemperateDutyPersoon(@RequestBody DeleteRequest request){
        return null;
    }


/// =====================================END===============================================================

    /**
     * 删除值班人员（仅管理员）
     */
    @PostMapping("/person/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteDutyPerson(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        Boolean result = dutyService.deleteDutyPerson(deleteRequest.getId());
        // 删除人员后，更新从今天开始的未来值班记录
        try {
            dutyRecordService.updateFutureDutyRecords(deleteRequest.getRemark());
        } catch (Exception e) {
            // 记录更新失败不影响删除操作
            e.printStackTrace();
        }
        return ResultUtils.success(result);
    }

    /**
     * 根据值班类型查询值班人员列表（仅管理员）
     */
    @PostMapping("/person/list/type")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<List<DutyPersonVO>> getDutyPersonListByType(@RequestBody DutyPersonQueryRequest request) {
        ThrowUtils.throwIf(request == null || request.getDutyType() == null, ErrorCode.PARAMS_ERROR);
        List<DutyPersonVO> list = dutyService.getDutyPersonListByType(request.getDutyType());
        return ResultUtils.success(list);
    }

    /**
     * 根据日期查询当天的值班人员列表
     * 所有用户都可以访问（用于日历显示）
     */
    @PostMapping("/person/list/date")
    public BaseResponse<List<DutyPersonVO>> getDutyPersonListByDate(@RequestBody DutyPersonByDateRequest request) {
        ThrowUtils.throwIf(request == null || request.getDate() == null, ErrorCode.PARAMS_ERROR);
        List<DutyPersonVO> list = dutyService.getDutyPersonListByDate(request.getDate());
        return ResultUtils.success(list);
    }

    /**
     * 统计用户的值班天数
     * 所有用户都可以访问（用于用户管理页面显示）
     */
    @PostMapping("/count")
    public BaseResponse<Integer> getDutyCount(@RequestBody DutyCountRequest request) {
        ThrowUtils.throwIf(request == null || request.getUserId() == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(request.getType() == null, ErrorCode.PARAMS_ERROR);
        Integer count = dutyService.getDutyCount(request.getUserId(), request.getType());
        return ResultUtils.success(count);
    }

    /**
     * 从指定日期开始更新值班配置（仅管理员）
     */
    @PostMapping("/person/update-from")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<List<DutyPersonVO>> updateDutyConfigFrom(@RequestBody DutyConfigUpdateFromRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        List<DutyPersonVO> updatedPersons = dutyService.updateDutyConfigFrom(request);
        return ResultUtils.success(updatedPersons);
    }
}

