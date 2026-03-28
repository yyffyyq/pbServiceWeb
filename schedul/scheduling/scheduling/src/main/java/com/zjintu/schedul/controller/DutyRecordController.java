package com.zjintu.schedul.controller;

import com.zjintu.schedul.annotation.AuthCheck;
import com.zjintu.schedul.common.BaseResponse;
import com.zjintu.schedul.common.ErrorCode;
import com.zjintu.schedul.common.ResultUtils;
import com.zjintu.schedul.constant.UserConstant;
import com.zjintu.schedul.exception.ThrowUtils;
import com.zjintu.schedul.model.vo.duptVO.DutyRecordVO;
import com.zjintu.schedul.service.DutyRecordService;
import jakarta.annotation.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * 值班记录管理控制器
 */
@RestController
@RequestMapping("/duty/record")
public class DutyRecordController {

    @Resource
    private DutyRecordService dutyRecordService;

    /**
     * 根据用户ID获取值班记录列表
     * @param userId 用户ID
     * @return 值班记录列表
     */
    @GetMapping("/list/{userId}")
    public BaseResponse<List<DutyRecordVO>> getDutyRecordsByUserId(@PathVariable Long userId) {
        ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR);
        List<DutyRecordVO> records = dutyRecordService.getDutyRecordsByUserId(userId);
        return ResultUtils.success(records);
    }

    /**
     * 生成指定日期范围的值班记录（仅管理员）
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 生成的记录数
     */
    @PostMapping("/generate")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Integer> generateDutyRecords(
            @RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam("endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        ThrowUtils.throwIf(startDate == null || endDate == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(startDate.after(endDate), ErrorCode.PARAMS_ERROR, "开始日期不能晚于结束日期");
        Integer count = dutyRecordService.generateDutyRecordsForDateRange(startDate, endDate);
        return ResultUtils.success(count);
    }

    /**
     * 根据日期范围获取值班记录（用于日历显示）
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 值班记录列表
     */
    @GetMapping("/range")
    public BaseResponse<List<DutyRecordVO>> getDutyRecordsByDateRange(
            @RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam("endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        ThrowUtils.throwIf(startDate == null || endDate == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(startDate.after(endDate), ErrorCode.PARAMS_ERROR, "开始日期不能晚于结束日期");
        List<DutyRecordVO> records = dutyRecordService.getDutyRecordsByDateRange(startDate, endDate);
        return ResultUtils.success(records);
    }
}

