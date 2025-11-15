package com.zjintu.schedul.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zjintu.schedul.model.entity.DutyPerson;
import org.apache.ibatis.annotations.Mapper;

/**
 * 值班人员Mapper
 */
@Mapper
public interface DutyPersonMapper extends BaseMapper<DutyPerson> {
}

