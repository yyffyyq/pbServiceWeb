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
import com.zjintu.schedul.mapper.DutyRecordMapper;
import com.zjintu.schedul.model.dto.duty.DutyConfigRequest;
import com.zjintu.schedul.model.dto.duty.DutyPersonAddRequest;
import com.zjintu.schedul.model.dto.duty.DutyConfigUpdateFromRequest;
import com.zjintu.schedul.model.entity.DutyConfig;
import com.zjintu.schedul.model.entity.DutyPerson;
import com.zjintu.schedul.model.entity.User;
import com.zjintu.schedul.model.entity.DutyRecord;
import com.zjintu.schedul.model.vo.DutyConfigVO;
import com.zjintu.schedul.model.vo.DutyPersonVO;
import com.zjintu.schedul.service.DutyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 值班服务实现类
 */
@Slf4j
@Service
public class DutyServiceImpl extends ServiceImpl<DutyPersonMapper, DutyPerson> implements DutyService {

    @Resource
    private DutyConfigMapper dutyConfigMapper;

    @Resource
    private DutyPersonMapper dutyPersonMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private DutyRecordMapper dutyRecordMapper;

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
        // 关键：避免 diffDays 为负数（当 targetDate < baseDate 时），取绝对值
        long diffWeeks = Math.abs(diffDays) / 7;

        // 周数差为偶数 → 单周；奇数 → 双周
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

    @Override
    public List<DutyPersonVO> updateDutyConfigFrom(DutyConfigUpdateFromRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(request.getStartDate() == null || request.getStartDate().trim().isEmpty(), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(request.getConfig() == null || request.getConfig().isEmpty(), ErrorCode.PARAMS_ERROR);

        try {
            // 解析开始日期（更新起点）
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date startDate = sdf.parse(request.getStartDate());

            // 截断开始日期的时间部分（确保按天对比）
            Calendar startCal = Calendar.getInstance();
            startCal.setTime(startDate);
            startCal.set(Calendar.HOUR_OF_DAY, 0);
            startCal.set(Calendar.MINUTE, 0);
            startCal.set(Calendar.SECOND, 0);
            startCal.set(Calendar.MILLISECOND, 0);
            Date actualStartDate = startCal.getTime();

            // 计算结束日期（未来1年，同样截断时间）
            Calendar endCal = Calendar.getInstance();
            endCal.setTime(actualStartDate);
            endCal.add(Calendar.YEAR, 1);
            // 关键：截断结束日期的时间部分，与 currentCal 格式一致
            endCal.set(Calendar.HOUR_OF_DAY, 0);
            endCal.set(Calendar.MINUTE, 0);
            endCal.set(Calendar.SECOND, 0);
            endCal.set(Calendar.MILLISECOND, 0);
            Date actualEndDate = endCal.getTime();

            Map<String, Object> config = request.getConfig();

            // 第一步：仅同步请求中存在的值班类型（不影响其他类型）
            syncDutyPersonsFromConfig(config);

            // 第二步：获取基准日期（用于判断单双周）
            QueryWrapper<DutyConfig> configWrapper = new QueryWrapper<>();
            configWrapper.eq("isDelete", 0);
            configWrapper.orderByDesc("createTime");
            configWrapper.last("LIMIT 1");
            DutyConfig dutyConfig = dutyConfigMapper.selectOne(configWrapper);
            Date baseDate = dutyConfig != null ? dutyConfig.getBaseDate() : null;

            // 第三步：遍历「指定日期及之后」的每一天（修复循环终止条件）
            Calendar currentCal = Calendar.getInstance();
            currentCal.setTime(actualStartDate); // 从指定日期开始

            // 关键：使用 Calendar 的 compareTo 方法对比日期（避免时间格式问题）
            while (currentCal.compareTo(endCal) <= 0) {
                Date currentDate = currentCal.getTime();
                int dayOfWeek = currentCal.get(Calendar.DAY_OF_WEEK); // 1=周日, 7=周六
                boolean isMonthEnd = isMonthEndDay(currentDate);

                // 1. 删除当天的所有值班记录（仅删除指定日期及之后的）
                QueryWrapper<DutyRecord> deleteWrapper = new QueryWrapper<>();
                deleteWrapper.apply("DATE(dutyDate) = DATE({0})", currentDate);
                deleteWrapper.eq("isDelete", 0);
                dutyRecordMapper.delete(deleteWrapper);

                // 2. 生成当天的值班记录（按类型查询DutyPerson表）
                List<Long> userIdsForToday = new ArrayList<>();

                // 月末最后两天：添加月末值班人员
                if (isMonthEnd) {
                    List<DutyPerson> monthEndPersons = dutyPersonMapper.selectList(
                            new QueryWrapper<DutyPerson>().eq("dutyType", "month_end").eq("isDelete", 0)
                    );
                    for (DutyPerson person : monthEndPersons) {
                        Long userId = person.getUserId();
                        if (userId != null && !userIdsForToday.contains(userId)) {
                            userIdsForToday.add(userId);
                            createDutyRecord(userId, currentDate, "month_end", request.getRemark());
                        }
                    }
                }

                // 工作日（周一到周五）：添加工作日值班人员
                if (dayOfWeek >= 2 && dayOfWeek <= 6) {
                    List<DutyPerson> weekdayPersons = dutyPersonMapper.selectList(
                            new QueryWrapper<DutyPerson>().eq("dutyType", "weekday").eq("isDelete", 0)
                    );
                    for (DutyPerson person : weekdayPersons) {
                        Long userId = person.getUserId();
                        if (userId != null && !userIdsForToday.contains(userId)) {
                            userIdsForToday.add(userId);
                            createDutyRecord(userId, currentDate, "weekday", request.getRemark());
                        }
                    }
                    // 关键：移动到下一天（必须在 continue 之前）
                    currentCal.add(Calendar.DAY_OF_MONTH, 1);
                    continue;
                }

                // 周六：添加对应单/双周组人员
                if (dayOfWeek == 7) {
                    boolean isSingle = baseDate != null ? isSingleWeek(currentDate, baseDate) : true;
                    String dutyType = isSingle ? "saturday_group1" : "saturday_group2";

                    List<DutyPerson> saturdayPersons = dutyPersonMapper.selectList(
                            new QueryWrapper<DutyPerson>().eq("dutyType", dutyType).eq("isDelete", 0)
                    );
                    for (DutyPerson person : saturdayPersons) {
                        Long userId = person.getUserId();
                        if (userId != null && !userIdsForToday.contains(userId)) {
                            userIdsForToday.add(userId);
                            createDutyRecord(userId, currentDate, dutyType, request.getRemark());
                        }
                    }
                    // 关键：移动到下一天（必须在 continue 之前）
                    currentCal.add(Calendar.DAY_OF_MONTH, 1);
                    continue;
                }

                // 周日：仅月末值班人员（已在上面处理），直接移动到下一天
                currentCal.add(Calendar.DAY_OF_MONTH, 1);
            }

            log.info("更新值班配置成功（处理 {} 至 {} 的日期）", sdf.format(actualStartDate), sdf.format(actualEndDate));
            return getDutyPersonListByDate(actualStartDate);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "更新值班配置失败");
        }
    }


    /**
     * 提取用户ID
     */
    private Long extractUserId(Map<String, Object> person) {
        Object idObj = person.get("id");
        if (idObj == null) {
            return null;
        }
        // 新增：处理字符串类型的ID
        if (idObj instanceof String) {
            try {
                return Long.parseLong((String) idObj);
            } catch (NumberFormatException e) {
                log.error("用户ID格式错误：{}", idObj);
                return null;
            }
        }
        if (idObj instanceof Integer) {
            return ((Integer) idObj).longValue();
        } else if (idObj instanceof Long) {
            return (Long) idObj;
        }
        return null;
    }

    /**
     * 创建值班记录
     */
    private void createDutyRecord(Long userId, Date dutyDate, String dutyType, String remark) {
        DutyRecord record = new DutyRecord();
        record.setUserId(userId);
        record.setDutyDate(dutyDate);
        record.setDutyType(dutyType);
        record.setStatus("normal");
        record.setRemark(remark);
        dutyRecordMapper.insert(record);
    }

    /**
     * 从请求配置中同步人员到 DutyPerson 表（不存在则插入）
     */
    private void syncDutyPersonsFromConfig(Map<String, Object> config) {
        // 1. 定义所有支持的类型映射（configKey -> 数据库dutyType）
        Map<String, String> typeMap = new HashMap<>();
        typeMap.put("weekdayDutyList", "weekday");
        typeMap.put("saturdayGroup1", "saturday_group1");
        typeMap.put("saturdayGroup2", "saturday_group2");
        typeMap.put("monthEndDutyList", "month_end");

        // 2. 只遍历请求中存在的 configKey（关键：不处理未传递的类型）
        for (Map.Entry<String, Object> configEntry : config.entrySet()) {
            String configKey = configEntry.getKey();
            // 跳过不支持的configKey（防止非法参数）
            if (!typeMap.containsKey(configKey)) {
                log.warn("不支持的值班类型配置key：{}，跳过处理", configKey);
                continue;
            }

            String dutyType = typeMap.get(configKey);
            Object personsObj = configEntry.getValue();
            // 校验值是否为人员列表（避免格式错误）
            if (!(personsObj instanceof List)) {
                log.error("{} 对应的配置不是列表格式，跳过处理", configKey);
                continue;
            }
            List<Map<String, Object>> persons = (List<Map<String, Object>>) personsObj;

            // 3. 处理当前类型：先删除该类型所有未删除的人员（全量替换）
            QueryWrapper<DutyPerson> deleteWrapper = new QueryWrapper<>();
            deleteWrapper.eq("dutyType", dutyType);
            deleteWrapper.eq("isDelete", 0);
            dutyPersonMapper.delete(deleteWrapper);

            // 4. 插入请求中的新人员（支持新增）
            for (Map<String, Object> person : persons) {
                Long userId = extractUserId(person);
                if (userId == null) continue;

                // 检查用户是否存在（避免插入无效用户）
                User user = userMapper.selectById(userId);
                if (user == null) {
                    log.warn("用户ID {} 不存在，跳过插入", userId);
                    continue;
                }

                // 插入新的值班人员配置
                DutyPerson dutyPerson = new DutyPerson();
                dutyPerson.setUserId(userId);
                dutyPerson.setDutyType(dutyType);
                dutyPersonMapper.insert(dutyPerson);
            }
        }
    }
}

