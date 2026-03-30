package com.zjintu.schedul.mapper.duty;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zjintu.schedul.model.dto.duty.DutyPersonTempareAddRequest;
import com.zjintu.schedul.model.entity.dupt.Duty;
import com.zjintu.schedul.model.entity.dupt.DutyPerson;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DutyMapper extends BaseMapper<Duty> {

    DutyPerson selectPersonByIdAndIsdelete(DutyPerson request);
}
