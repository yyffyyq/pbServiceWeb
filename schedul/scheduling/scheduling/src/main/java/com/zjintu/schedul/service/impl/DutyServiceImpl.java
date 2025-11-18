package com.zjintu.schedul.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zjintu.schedul.common.ErrorCode;
import com.zjintu.schedul.exception.BusinessException;
import com.zjintu.schedul.exception.ThrowUtils;
import com.zjintu.schedul.mapper.DutyConfigMapper;
import com.zjintu.schedul.mapper.DutyPersonMapper;
import com.zjintu.schedul.mapper.UserMapper;
import com.zjintu.schedul.model.dto.duty.DutyConfigRequest;
import com.zjintu.schedul.model.dto.duty.DutyPersonAddRequest;
import com.zjintu.schedul.model.entity.DutyConfig;
import com.zjintu.schedul.model.entity.DutyPerson;
import com.zjintu.schedul.model.entity.User;
import com.zjintu.schedul.model.vo.DutyConfigVO;
import com.zjintu.schedul.model.vo.DutyPersonVO;
import com.zjintu.schedul.service.DutyService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 值班服务实现类
 */
@Service
public class DutyServiceImpl extends ServiceImpl<DutyPersonMapper, DutyPerson> implements DutyService {

    @Resource
    private DutyConfigMapper dutyConfigMapper;

    @Resource
    private DutyPersonMapper dutyPersonMapper;

    @Resource
    private UserMapper userMapper;

    @Override
    public DutyConfigVO getDutyConfig() {
        DutyConfigVO configVO = new DutyConfigVO();
        // 获取基准日期
        QueryWrapper<DutyConfig> configWrapper = new QueryWrapper<>();
        configWrapper.eq("isDelete", 0);
        configWrapper.orderByDesc("createTime");
        configWrapper.last("LIMIT 1");
        DutyConfig config = dutyConfigMapper.selectOne(configWrapper);
        if (config != null) {
            configVO.setBaseDate(config.getBaseDate());
        }

        // 获取工作日值班人员
        configVO.setWeekdayDutyList(getDutyPersonListByType("weekday"));

        // 获取周六单周组
        configVO.setSaturdayGroup1(getDutyPersonListByType("saturday_group1"));

        // 获取周六双周组
        configVO.setSaturdayGroup2(getDutyPersonListByType("saturday_group2"));

        // 获取月末值班人员
        configVO.setMonthEndDutyList(getDutyPersonListByType("month_end"));

        return configVO;
    }

    @Override
    public Boolean saveBaseDate(DutyConfigRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);

        // 查询是否已存在配置
        QueryWrapper<DutyConfig> wrapper = new QueryWrapper<>();
        wrapper.eq("isDelete", 0);
        wrapper.orderByDesc("createTime");
        wrapper.last("LIMIT 1");
        DutyConfig existingConfig = dutyConfigMapper.selectOne(wrapper);

        if (existingConfig != null) {
            // 更新现有配置
            existingConfig.setBaseDate(request.getBaseDate());
            int result = dutyConfigMapper.updateById(existingConfig);
            return result > 0;
        } else {
            // 创建新配置
            DutyConfig config = new DutyConfig();
            config.setBaseDate(request.getBaseDate());
            int result = dutyConfigMapper.insert(config);
            return result > 0;
        }
    }

    @Override
    public Long addDutyPerson(DutyPersonAddRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(request.getUserId() == null || request.getUserId() <= 0, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(request.getDutyType() == null || request.getDutyType().trim().isEmpty(), ErrorCode.PARAMS_ERROR);

        // 验证值班类型
        String dutyType = request.getDutyType();
        if (!dutyType.equals("weekday") && !dutyType.equals("saturday_group1") 
            && !dutyType.equals("saturday_group2") && !dutyType.equals("month_end")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "值班类型不正确");
        }

        // 检查用户是否存在
        User user = userMapper.selectById(request.getUserId());
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");

        // 检查是否已存在相同的配置
        QueryWrapper<DutyPerson> wrapper = new QueryWrapper<>();
        wrapper.eq("userId", request.getUserId());
        wrapper.eq("dutyType", request.getDutyType());
        wrapper.eq("isDelete", 0);
        DutyPerson existing = dutyPersonMapper.selectOne(wrapper);
        if (existing != null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该用户已在此值班类型中");
        }

        // 添加值班人员
        DutyPerson dutyPerson = new DutyPerson();
        dutyPerson.setUserId(request.getUserId());
        dutyPerson.setDutyType(request.getDutyType());
        int result = dutyPersonMapper.insert(dutyPerson);
        ThrowUtils.throwIf(result <= 0, ErrorCode.OPERATION_ERROR);
        return dutyPerson.getId();
    }

    @Override
    public Boolean deleteDutyPerson(Long id) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        boolean result = this.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return true;
    }

    @Override
    public List<DutyPersonVO> getDutyPersonListByType(String dutyType) {
        QueryWrapper<DutyPerson> wrapper = new QueryWrapper<>();
        wrapper.eq("dutyType", dutyType);
        wrapper.eq("isDelete", 0);
        List<DutyPerson> dutyPersonList = dutyPersonMapper.selectList(wrapper);

        List<DutyPersonVO> voList = new ArrayList<>();
        for (DutyPerson dutyPerson : dutyPersonList) {
            User user = userMapper.selectById(dutyPerson.getUserId());
            if (user != null) {
                DutyPersonVO vo = new DutyPersonVO();
                vo.setDutyPersonId(dutyPerson.getId()); // 设置duty_person表的ID
                vo.setId(user.getId());
                vo.setUserName(user.getUserName());
                vo.setDept(user.getDept());
                vo.setPhone(user.getPhone());
                vo.setDutyType(dutyPerson.getDutyType());
                voList.add(vo);
            }
        }
        return voList;
    }

    @Override
    public List<DutyPersonVO> getDutyPersonListByDate(Date date) {
        ThrowUtils.throwIf(date == null, ErrorCode.PARAMS_ERROR);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK); // 1-7, 1是周日

        List<DutyPersonVO> result = new ArrayList<>();

        // 检查是否为月末最后两天
        boolean isMonthEnd = isMonthEndDay(date);
        
        // 如果是月末最后两天，添加月末值班人员
        if (isMonthEnd) {
            result.addAll(getDutyPersonListByType("month_end"));
        }

        // 周一到周五（2-6）：添加工作日值班人员
        if (dayOfWeek >= 2 && dayOfWeek <= 6) {
            result.addAll(getDutyPersonListByType("weekday"));
            return result;
        }

        // 如果是月末但是周六，还需要添加周六值班人员
        if (dayOfWeek == 7) {
            // 获取基准日期
            QueryWrapper<DutyConfig> configWrapper = new QueryWrapper<>();
            configWrapper.eq("isDelete", 0);
            configWrapper.orderByDesc("createTime");
            configWrapper.last("LIMIT 1");
            DutyConfig config = dutyConfigMapper.selectOne(configWrapper);

            if (config != null && config.getBaseDate() != null) {
                // 计算是否为单周
                boolean isSingle = isSingleWeek(date, config.getBaseDate());
                if (isSingle) {
                    result.addAll(getDutyPersonListByType("saturday_group1"));
                } else {
                    result.addAll(getDutyPersonListByType("saturday_group2"));
                }
            } else {
                // 如果没有基准日期，默认返回单周组
                result.addAll(getDutyPersonListByType("saturday_group1"));
            }
            return result;
        }

        // 周日（1）：只返回月末值班人员（如果是月末的话）
        return result;
    }

    /**
     * 判断指定日期是单周还是双周
     */
    private boolean isSingleWeek(Date targetDate, Date baseDate) {
        Calendar base = Calendar.getInstance();
        base.setTime(baseDate);
        base.set(Calendar.HOUR_OF_DAY, 0);
        base.set(Calendar.MINUTE, 0);
        base.set(Calendar.SECOND, 0);
        base.set(Calendar.MILLISECOND, 0);

        Calendar target = Calendar.getInstance();
        target.setTime(targetDate);
        target.set(Calendar.HOUR_OF_DAY, 0);
        target.set(Calendar.MINUTE, 0);
        target.set(Calendar.SECOND, 0);
        target.set(Calendar.MILLISECOND, 0);

        long diffTime = target.getTimeInMillis() - base.getTimeInMillis();
        long diffDays = diffTime / (1000 * 60 * 60 * 24);
        long diffWeeks = diffDays / 7;

        // 如果周数差是偶数，则是单周；如果是奇数，则是双周
        return diffWeeks % 2 == 0;
    }

    /**
     * 判断是否为月末最后两天
     */
    private boolean isMonthEndDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // 获取该月的最后一天
        Calendar lastDayCalendar = Calendar.getInstance();
        lastDayCalendar.set(year, month, 1);
        int lastDay = lastDayCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        // 判断是否为最后一天或倒数第二天
        return day == lastDay || day == lastDay - 1;
    }

    @Override
    public Integer getDutyCount(Long userId, String type) {
        ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(type == null || (!type.equals("month") && !type.equals("year")), ErrorCode.PARAMS_ERROR);

        // 检查用户是否在值班配置中
        QueryWrapper<DutyPerson> wrapper = new QueryWrapper<>();
        wrapper.eq("userId", userId);
        wrapper.eq("isDelete", 0);
        List<DutyPerson> dutyPersonList = dutyPersonMapper.selectList(wrapper);

        if (dutyPersonList == null || dutyPersonList.isEmpty()) {
            return 0;
        }

        // 获取用户的值班类型
        boolean isWeekdayDuty = dutyPersonList.stream().anyMatch(dp -> "weekday".equals(dp.getDutyType()));
        boolean isSaturdayGroup1 = dutyPersonList.stream().anyMatch(dp -> "saturday_group1".equals(dp.getDutyType()));
        boolean isSaturdayGroup2 = dutyPersonList.stream().anyMatch(dp -> "saturday_group2".equals(dp.getDutyType()));
        boolean isMonthEndDuty = dutyPersonList.stream().anyMatch(dp -> "month_end".equals(dp.getDutyType()));

        if (!isWeekdayDuty && !isSaturdayGroup1 && !isSaturdayGroup2 && !isMonthEndDuty) {
            return 0;
        }

        // 获取基准日期
        QueryWrapper<DutyConfig> configWrapper = new QueryWrapper<>();
        configWrapper.eq("isDelete", 0);
        configWrapper.orderByDesc("createTime");
        configWrapper.last("LIMIT 1");
        DutyConfig config = dutyConfigMapper.selectOne(configWrapper);
        Date baseDate = config != null ? config.getBaseDate() : null;

        // 计算时间范围
        Calendar now = Calendar.getInstance();
        int currentYear = now.get(Calendar.YEAR);
        int currentMonth = now.get(Calendar.MONTH);

        Calendar startDate = Calendar.getInstance();
        Calendar endDate = Calendar.getInstance();

        if ("month".equals(type)) {
            // 本月
            startDate.set(currentYear, currentMonth, 1, 0, 0, 0);
            startDate.set(Calendar.MILLISECOND, 0);
            endDate.set(currentYear, currentMonth, now.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59);
            endDate.set(Calendar.MILLISECOND, 999);
        } else {
            // 本年
            startDate.set(currentYear, 0, 1, 0, 0, 0);
            startDate.set(Calendar.MILLISECOND, 0);
            endDate.set(currentYear, 11, 31, 23, 59, 59);
            endDate.set(Calendar.MILLISECOND, 999);
        }

        // 获取今天的日期（忽略时间部分）
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        // 计算值班天数（只统计已过去的日期）
        int count = 0;
        Calendar currentDate = (Calendar) startDate.clone();
        currentDate.set(Calendar.HOUR_OF_DAY, 0);
        currentDate.set(Calendar.MINUTE, 0);
        currentDate.set(Calendar.SECOND, 0);
        currentDate.set(Calendar.MILLISECOND, 0);

        while (currentDate.before(endDate) || currentDate.equals(endDate)) {
            // 只统计当前日期之前的日期（不包括今天）
            if (currentDate.before(today)) {
                int dayOfWeek = currentDate.get(Calendar.DAY_OF_WEEK); // 1-7, 1是周日
                boolean counted = false;

                // 检查是否为月末最后两天
                boolean isMonthEndDay = isMonthEndDay(currentDate.getTime());
                
                // 如果是月末且用户在月末值班列表中，则计数
                if (isMonthEndDay && isMonthEndDuty) {
                    count++;
                    counted = true;
                }
                
                // 周一到周五（2-6）：如果用户在工作日值班列表中，则计数
                if (dayOfWeek >= 2 && dayOfWeek <= 6 && isWeekdayDuty) {
                    count++;
                    counted = true;
                }
                
                // 周六（7）：根据单周/双周判断
                if (!counted && dayOfWeek == 7) {
                    if (baseDate != null) {
                        boolean isSingle = isSingleWeek(currentDate.getTime(), baseDate);
                        if ((isSingle && isSaturdayGroup1) || (!isSingle && isSaturdayGroup2)) {
                            count++;
                        }
                    } else {
                        // 如果没有基准日期，默认单周组
                        if (isSaturdayGroup1) {
                            count++;
                        }
                    }
                }
                // 周日（1）：不安排值班，不计数
            }
            // 移动到下一天
            currentDate.add(Calendar.DAY_OF_MONTH, 1);
        }

        return count;
    }
}

