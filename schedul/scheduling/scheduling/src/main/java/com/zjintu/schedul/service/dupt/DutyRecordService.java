package com.zjintu.schedul.service.dupt;


import com.baomidou.mybatisplus.extension.service.IService;
import com.zjintu.schedul.model.dto.duty.DutyRecordTempareRequest;
import com.zjintu.schedul.model.entity.dupt.DutyRecord;
import com.zjintu.schedul.model.vo.duptVO.DutyRecordTempareVO;
import com.zjintu.schedul.model.vo.duptVO.DutyRecordVO;

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

    /**
     * 更新临时组排班记录
     * @param currentDate 插入临时组排班日期
     * @param userId 增加记录人员id
     */
    String updateTheDayDutyRecord(Date currentDate,Long userId,String type_group);

    /**
     * 通过部门编号、查询时间确定返回临时值班组人员
     * @param deptId 部门编号
     * @param dutyRecordTempareRequest 请求日期
     * @return
     */
    List<DutyRecordTempareVO> selectUserDuptTypeBydeptIdAndDuptDate(Long deptId, DutyRecordTempareRequest dutyRecordTempareRequest);

    /**
     * 获取用户临时组用户编号列表
     * @param currentDate 查看日期
     * @param typeGroup 查看分组
     * @return 返回用户id列表
     */
    List<Long> selectUserIdByGroupAndDate(Date currentDate, String typeGroup);

    /**
     * 删除临时组值班人员信息
     * @param currentDate 插入临时组排班日期
     * @param deleteUserId 增加记录人员id
     * @param typeGroup 修改分组
     * @return 返回修改人员信息
     */
    String deleteTheDayDutyRecord(Date currentDate, Long deleteUserId, String typeGroup);
}
