package com.zjintu.schedul.controller;

import com.zjintu.schedul.common.BaseResponse;
import com.zjintu.schedul.common.ErrorCode;
import com.zjintu.schedul.common.ResultUtils;
import com.zjintu.schedul.exception.ThrowUtils;
import com.zjintu.schedul.model.vo.DutyRecordVO;
import com.zjintu.schedul.service.DutyRecordService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
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
}

