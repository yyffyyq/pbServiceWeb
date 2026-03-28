package com.zjintu.schedul.service.impl.dutyDay;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zjintu.schedul.mapper.DutyHolidayMapper;
import com.zjintu.schedul.mapper.dutyDay.DutyWorkdayMapper;
import com.zjintu.schedul.model.entity.DutyDay.DutyHoliday;
import com.zjintu.schedul.model.entity.DutyDay.DutyWorkday;
import com.zjintu.schedul.service.DutyHolidayService;
import com.zjintu.schedul.service.duptDay.DutyWorkdayService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class DutyWorkdyServiceImpl extends ServiceImpl<DutyWorkdayMapper, DutyWorkday> implements DutyWorkdayService {
    @Resource
    private DutyWorkdayMapper dutyWorkdayMapper;



}
