package com.zjintu.schedul.utils;

import java.util.Calendar;
import java.util.Date;

public class ClearDateUtils {
    /**
     * 将日期转换为当天的 00:00:00，确保与数据库匹配
     */
    public static Date atStartOfDay(Date date) {
        if (date == null) return null;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }
}
