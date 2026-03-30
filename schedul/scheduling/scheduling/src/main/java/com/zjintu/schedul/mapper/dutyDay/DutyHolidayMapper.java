package com.zjintu.schedul.mapper.dutyDay;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zjintu.schedul.model.entity.DutyDay.DutyHoliday;
import io.lettuce.core.dynamic.annotation.Param;

public interface DutyHolidayMapper extends BaseMapper<DutyHoliday> {

    // 查找is_delete为1的日期进行修改
    DutyHoliday selectDeletedDate(@Param("formattedDate")String formattedDate);

    // 恢复逻辑删除节假日信息
    int updateByHolidayId(@Param("id")Long updateId);
}
