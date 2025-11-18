package com.zjintu.schedul.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zjintu.schedul.mapper.DutyRecordMapper;
import com.zjintu.schedul.model.entity.DutyRecord;
import com.zjintu.schedul.model.vo.DutyPersonVO;
import com.zjintu.schedul.model.vo.DutyRecordVO;
import com.zjintu.schedul.service.DutyRecordService;
import com.zjintu.schedul.service.DutyService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author Administrator
* @description 针对表【duty_record(值班记录表)】的数据库操作Service实现
* @createDate 2025-11-17 09:53:28
*/
@Service
public class DutyRecordServiceImpl extends ServiceImpl<DutyRecordMapper, DutyRecord>
    implements DutyRecordService {

    @Resource
    private DutyService dutyService;

    @Override
    public List<DutyRecordVO> getDutyRecordsByUserId(Long userId) {
        QueryWrapper<DutyRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        queryWrapper.orderByDesc("dutyDate");
        List<DutyRecord> records = this.list(queryWrapper);
        
        return records.stream().map(record -> {
            DutyRecordVO vo = new DutyRecordVO();
            BeanUtils.copyProperties(record, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer generateDutyRecordsForDate(Date date) {
        if (date == null) {
            return 0;
        }

        // 获取当天的值班人员列表
        List<DutyPersonVO> dutyPersons = dutyService.getDutyPersonListByDate(date);
        if (dutyPersons == null || dutyPersons.isEmpty()) {
            return 0;
        }

        int count = 0;
        for (DutyPersonVO person : dutyPersons) {
            // 检查是否已存在记录
            QueryWrapper<DutyRecord> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userId", person.getId());
            queryWrapper.eq("dutyDate", date);
            queryWrapper.eq("isDelete", 0);
            DutyRecord existing = this.getOne(queryWrapper);

            if (existing == null) {
                // 创建新记录
                DutyRecord record = new DutyRecord();
                record.setUserId(person.getId());
                record.setDutyDate(date);
                record.setDutyType(person.getDutyType());
                record.setStatus("normal");
                this.save(record);
                count++;
            }
        }

        return count;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer generateDutyRecordsForDateRange(Date startDate, Date endDate) {
        if (startDate == null || endDate == null) {
            return 0;
        }

        int totalCount = 0;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        Calendar endCal = Calendar.getInstance();
        endCal.setTime(endDate);
        endCal.set(Calendar.HOUR_OF_DAY, 0);
        endCal.set(Calendar.MINUTE, 0);
        endCal.set(Calendar.SECOND, 0);
        endCal.set(Calendar.MILLISECOND, 0);

        while (!calendar.after(endCal)) {
            Date currentDate = calendar.getTime();
            int count = generateDutyRecordsForDate(currentDate);
            totalCount += count;
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        return totalCount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer updateFutureDutyRecords() {
        // 获取今天的日期（从今天开始更新）
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        Date todayDate = today.getTime();

        // 删除今天及以后的所有值班记录
        QueryWrapper<DutyRecord> deleteWrapper = new QueryWrapper<>();
        deleteWrapper.ge("duty_date", todayDate);
        deleteWrapper.eq("is_delete", 0);
        this.remove(deleteWrapper);

        // 重新生成未来一个月的值班记录（根据新的配置）
        Calendar endDate = Calendar.getInstance();
        endDate.setTime(todayDate);
        endDate.add(Calendar.MONTH, 1);

        return generateDutyRecordsForDateRange(todayDate, endDate.getTime());
    }
}




