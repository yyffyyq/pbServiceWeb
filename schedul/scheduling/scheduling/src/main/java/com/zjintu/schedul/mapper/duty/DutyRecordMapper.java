package com.zjintu.schedul.mapper.duty;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zjintu.schedul.model.entity.dupt.DutyRecord;
import io.lettuce.core.dynamic.annotation.Param;
import org.apache.ibatis.annotations.Mapper;

import java.util.Date;
import java.util.List;

/**
* @author Administrator
* @description 针对表【duty_record(值班记录表)】的数据库操作Mapper
* @createDate 2025-11-17 09:53:28
* @Entity generator.domain.DutyRecord
*/
@Mapper
public interface DutyRecordMapper extends BaseMapper<DutyRecord> {


    List<Long> selectUserIdList(Date currentDate, String typeGroup);

    int addRecord(Date currentDate, Long userId);

    DutyRecord selectDutyRecord(DutyRecord record);

    boolean updateIsdelete(DutyRecord record);

    /**
     * 获取该日值班人员值班名称
     * @param userId 用户编号
     * @param currentDate 值班日期
     * @return 值班名称
     */
    String selectByUserIdAndDutyDate(Long userId, Date currentDate);

    Long deleteUserIdById(@Param("userId") Long deleteUserId,  // 必须改成 Long！
                         @Param("dutyType") String typeGroup,
                         @Param("dutyDate") Date currentDate);
}




