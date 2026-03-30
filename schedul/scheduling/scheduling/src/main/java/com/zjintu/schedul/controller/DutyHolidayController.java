package com.zjintu.schedul.controller;

import com.zjintu.schedul.common.BaseResponse;
import com.zjintu.schedul.common.ErrorCode;
import com.zjintu.schedul.common.ResultUtils;
import com.zjintu.schedul.exception.ThrowUtils;
import com.zjintu.schedul.model.dto.duty.DutyHolidayRequest;
import com.zjintu.schedul.model.entity.DutyDay.DutyHoliday;
import com.zjintu.schedul.model.entity.DutyDay.DutyWorkday;
import com.zjintu.schedul.model.vo.dutyDayVO.DutyHolidayVO;
import com.zjintu.schedul.model.vo.dutyDayVO.DutyWorkdayVO;
import com.zjintu.schedul.service.dupt.DutyHolidayService;
import com.zjintu.schedul.service.duptDay.DutyWorkdayService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/duty/holiday")
public class DutyHolidayController {
    @Resource
    private DutyHolidayService dutyHolidayService;

    @Resource
    private DutyWorkdayService dutyWorkdayService;

    /**
     * 获取假期日期
     * @return YY-MM-DD假期列表
     */
    @GetMapping("/getlist")
    public BaseResponse<ArrayList<DutyHolidayVO>> getlist() {
        ArrayList<DutyHolidayVO> dutyHolidayVOs = new ArrayList<>();
        List<DutyHoliday> holidayList = dutyHolidayService.list();
        for(DutyHoliday dutyHoliday : holidayList) {
            DutyHolidayVO dutyHolidayVO = new DutyHolidayVO();
            BeanUtils.copyProperties(dutyHoliday, dutyHolidayVO);
            dutyHolidayVOs.add(dutyHolidayVO);
        }
        return ResultUtils.success(dutyHolidayVOs);
    }

    /**
     * 手动设置添加假期
     * @param dutyHolidayRequest 日期请求YY-MM-DD
     * @return 成功或者失败
     */
    @PostMapping("/setHoliday")
    public BaseResponse<String> setHoliday(@RequestBody DutyHolidayRequest dutyHolidayRequest) {
        ThrowUtils.throwIf(dutyHolidayRequest == null, ErrorCode.PARAMS_ERROR);
        String result = dutyHolidayService.add(dutyHolidayRequest);
        return ResultUtils.success(result);
    }

    @PostMapping("/deleteHoliday")
    @Operation(summary = "查询获取假期日期",description = "查询补班信息用于前段获取")
    public BaseResponse<String> deleteHoliday(@RequestBody DutyHolidayRequest dutyHolidayRequest) {
        ThrowUtils.throwIf(dutyHolidayRequest == null, ErrorCode.PARAMS_ERROR);
        String result = dutyHolidayService.delete(dutyHolidayRequest);
        return ResultUtils.success(result);

    }


    @GetMapping("/getList/workday")
    @Operation(summary = "查询获取补班日期",description = "查询补班信息用于前段获取")
    public BaseResponse<ArrayList<DutyWorkdayVO>> getListWorkday() {
        ArrayList<DutyWorkdayVO> dutyWorkdayVOs = new ArrayList<>();
        List<DutyWorkday> dutyWorkdayList =  dutyWorkdayService.list();
        for(DutyWorkday dutyWorkday : dutyWorkdayList) {
            DutyWorkdayVO dutyWorkdayVO = new DutyWorkdayVO();
            BeanUtils.copyProperties(dutyWorkday, dutyWorkdayVO);
            dutyWorkdayVOs.add(dutyWorkdayVO);
        }
        return ResultUtils.success(dutyWorkdayVOs);
    }
}
