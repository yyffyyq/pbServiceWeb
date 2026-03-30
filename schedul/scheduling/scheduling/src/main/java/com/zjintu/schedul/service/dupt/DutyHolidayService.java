package com.zjintu.schedul.service.dupt;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zjintu.schedul.model.dto.duty.DutyHolidayRequest;
import com.zjintu.schedul.model.entity.DutyDay.DutyHoliday;

public interface DutyHolidayService extends IService<DutyHoliday> {
    String add(DutyHolidayRequest dutyHolidayRequest);

    String delete(DutyHolidayRequest dutyHolidayRequest);
}
