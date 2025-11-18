package com.zjintu.schedul.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.zjintu.schedul.model.entity.DutyRecord;
import com.zjintu.schedul.model.vo.DutyRecordVO;

import java.util.Date;
import java.util.List;

/**
* @author Administrator
* @description 针对表【duty_record(值班记录表)】的数据库操作Service
* @createDate 2025-11-17 09:53:28
*/
public interface DutyRecordService extends IService<DutyRecord> {

    /**
     * 根据用户ID获取值班记录列表
     * @param userId 用户ID
     * @return 值班记录列表
     */
    List<DutyRecordVO> getDutyRecordsByUserId(Long userId);

    /**
     * 生成指定日期的值班记录
     * @param date 日期
     * @return 生成的记录数
     */
    Integer generateDutyRecordsForDate(Date date);

    /**
     * 生成指定日期范围的值班记录
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 生成的记录数
     */
    Integer generateDutyRecordsForDateRange(Date startDate, Date endDate);

    /**
     * 生成指定日期范围的值班记录
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param remark 调班备注
     * @return 生成的记录数
     */
    Integer generateDutyRecordsForDateRange(Date startDate, Date endDate, String remark);

    /**
     * 更新从今天开始的未来值班记录（用于配置变更后更新未来排班）
     * @param remark 调班备注
     * @return 更新的记录数
     */
    Integer updateFutureDutyRecords(String remark);

    /**
     * 根据日期范围获取值班记录
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 值班记录列表
     */
    List<DutyRecordVO> getDutyRecordsByDateRange(Date startDate, Date endDate);
}
