package com.zjintu.schedul.mapper.duty;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zjintu.schedul.model.entity.dupt.DutyPerson;

/**
 * 值班人员Mapper
 */

public interface DutyPersonMapper extends BaseMapper<DutyPerson> {
    boolean updateByPersonIdIsDelete(DutyPerson dutyPerson);
}

