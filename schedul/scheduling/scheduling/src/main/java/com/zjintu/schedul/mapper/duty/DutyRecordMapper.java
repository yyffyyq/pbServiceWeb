package com.zjintu.schedul.mapper.duty;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zjintu.schedul.model.entity.dupt.DutyRecord;
import org.apache.ibatis.annotations.Mapper;

import java.util.Date;

/**
* @author Administrator
* @description 针对表【duty_record(值班记录表)】的数据库操作Mapper
* @createDate 2025-11-17 09:53:28
* @Entity generator.domain.DutyRecord
*/
@Mapper
public interface DutyRecordMapper extends BaseMapper<DutyRecord> {


    int addRecord(Date currentDate, Long userId);

    DutyRecord selectDutyRecord(DutyRecord record);

    boolean updateIsdelete(DutyRecord record);
}




