package com.zjintu.schedul.job;

import com.zjintu.schedul.service.dupt.DutyRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


import java.util.Calendar;
import java.util.Date;

/**
 * 值班记录自动生成定时任务
 * 每天凌晨2点自动生成当天的值班记录
 */
@Component
@Slf4j
public class DutyRecordGenerateJob {

    @Autowired
    private DutyRecordService dutyRecordService;

    /**
     * 每天凌晨2点执行，生成当天的值班记录
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void generateTodayDutyRecords() {
        log.info("开始执行定时任务：生成今天的值班记录");
        try {
            Date today = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(today);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            
            Date todayDate = calendar.getTime();
            Integer count = dutyRecordService.generateDutyRecordsForDate(todayDate);
            log.info("定时任务执行成功：生成了 {} 条值班记录", count);
        } catch (Exception e) {
            log.error("定时任务执行失败：生成值班记录时出错", e);
        }
    }
}
