package com.zjintu.schedul.service.dupt.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zjintu.schedul.common.ErrorCode;
import com.zjintu.schedul.exception.BusinessException;
import com.zjintu.schedul.mapper.dutyDay.DutyHolidayMapper;
import com.zjintu.schedul.model.dto.duty.DutyHolidayRequest;
import com.zjintu.schedul.model.entity.DutyDay.DutyHoliday;
import com.zjintu.schedul.service.dupt.DutyHolidayService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Service
public class DutyHolidayServiceImpl extends ServiceImpl<DutyHolidayMapper, DutyHoliday> implements DutyHolidayService {


    @Resource
    private DutyHolidayMapper dutyHolidayMapper;
    @Override
    public String add(DutyHolidayRequest dutyHolidayRequest) {
        // 创建假期对象
        DutyHoliday dutyHoliday = new DutyHoliday();
        // 拿到设置休息日期
        String holidayDate = dutyHolidayRequest.getHolidayDate();
        // 修改格式为 YY-MM-DD
        String formattedDate;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        try {
            // 尝试解析并重新格式化（这会自动处理 2026-3-7 变为 2026-03-07 的补位）
            LocalDate date = LocalDate.parse(holidayDate.replace("/", "-").replace(".", "-"),
                    DateTimeFormatter.ofPattern("yyyy[-M][-d]"));
            formattedDate = date.format(formatter);
        } catch (DateTimeParseException e) {
            throw new BusinessException(ErrorCode.HOLIDAY_PARAMS_ERROR);
        }
        DutyHoliday existingHoliday = dutyHolidayMapper.selectOne(
                new QueryWrapper<DutyHoliday>()
                        .eq("holiday_date", formattedDate)
                        .last("limit 1")
        );
        if (existingHoliday != null) {
            // 日期已存在，直接返回提示或抛出异常
            throw new BusinessException(ErrorCode.EXIT_HOLIDAY_READY_ERROR);
        }
        // 查询删除后的
        DutyHoliday existingHolidayisdeleted = dutyHolidayMapper.selectDeletedDate(formattedDate);
        if(existingHolidayisdeleted != null) {
            Long updateId = existingHolidayisdeleted.getId();
            int Result =  dutyHolidayMapper.updateByHolidayId(updateId);
            return "成功" + Result;
        }
        // 设置修改类型为手动清除
        dutyHoliday.setType(1);
        dutyHoliday.setHolidayDate(formattedDate);
        // 创建数据库查询对象，加入数据库
        // 执行数据库
        int result = dutyHolidayMapper.insert(dutyHoliday);
        // 返回是否操作成功值
        if (result > 0) {
            return "成功";
        }else{
            return "失败";
        }
    }

    @Override
    public String delete(DutyHolidayRequest dutyHolidayRequest) {
        // 1. 增加非空校验
        if (dutyHolidayRequest == null || dutyHolidayRequest.getHolidayDate() == null) {
            return "请求参数错误";
        }

        // 获取日期
        String holidayDate = dutyHolidayRequest.getHolidayDate();

        QueryWrapper<DutyHoliday> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("holiday_date", holidayDate);

        int result = dutyHolidayMapper.delete(queryWrapper);

        // 3. 返回结果
        if (result > 0) {
            return "删除成功";
        } else {
            // 如果返回 0，说明数据库里本来就没有这个日期的记录
            throw new BusinessException(ErrorCode.NO_DATE_DELETE_ERROR);
        }
    }
}
