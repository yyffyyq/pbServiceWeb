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
import com.zjintu.schedul.service.DutyRecordService;
import com.zjintu.schedul.service.DutyService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import java.util.List;

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

