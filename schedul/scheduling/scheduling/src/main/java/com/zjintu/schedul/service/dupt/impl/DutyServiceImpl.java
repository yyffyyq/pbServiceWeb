package com.zjintu.schedul.service.dupt.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zjintu.schedul.common.ErrorCode;
import com.zjintu.schedul.exception.BusinessException;
import com.zjintu.schedul.exception.ThrowUtils;
import com.zjintu.schedul.mapper.duty.DutyConfigMapper;
import com.zjintu.schedul.mapper.duty.DutyMapper;
import com.zjintu.schedul.mapper.duty.DutyPersonMapper;
import com.zjintu.schedul.mapper.duty.DutyRecordMapper;
import com.zjintu.schedul.mapper.user.UserMapper;
import com.zjintu.schedul.model.dto.duty.DutyConfigRequest;
import com.zjintu.schedul.model.dto.duty.DutyPersonAddRequest;
import com.zjintu.schedul.model.dto.duty.DutyConfigUpdateFromRequest;
import com.zjintu.schedul.model.dto.duty.DutyPersonTempareAddRequest;
import com.zjintu.schedul.model.entity.dupt.DutyConfig;
import com.zjintu.schedul.model.entity.dupt.DutyPerson;
import com.zjintu.schedul.model.entity.user.User;
import com.zjintu.schedul.model.entity.dupt.DutyRecord;
import com.zjintu.schedul.model.entity.dupt.Duty;
import com.zjintu.schedul.model.vo.duptVO.DeptVO;
import com.zjintu.schedul.model.vo.duptVO.DutyConfigVO;
import com.zjintu.schedul.model.vo.duptVO.DutyPersonVO;
import com.zjintu.schedul.service.dupt.DutyService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.DayOfWeek;
import java.time.temporal.ChronoUnit;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoField;
import java.util.*;

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

    @Resource
    private DutyMapper dutyMapper;

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

        // todo获取临时值班人员
        configVO.setGroupeTemporaireList(getDutyPersonListByType("groupe_temporaire"));

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

    /**
     * 添加值班分组人员
     * @param request 添加值班人员请求
     * @return
     */
    @Override
    public Long addDutyPerson(DutyPersonAddRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(request.getUserId() == null || request.getUserId() <= 0, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(request.getDutyType() == null || request.getDutyType().trim().isEmpty(), ErrorCode.PARAMS_ERROR);

        // 验证值班类型
        // 工作日值班、单周组、双周组、月末两天组、临时值班添加组
        String dutyType = request.getDutyType();
        if (!dutyType.equals("weekday")
                && !dutyType.equals("saturday_group1")
                && !dutyType.equals("saturday_group2")
                && !dutyType.equals("month_end")
                && !dutyType.equals("groupe_temporaire")) {
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
                // 🚨 黄金修改点：不要默默兜底，直接抛出业务异常阻断流程！
                log.error("生成排班失败：数据库 duty_config 表中缺少基准日期(baseDate)配置！");
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "系统缺少单双周基准日期配置，请先联系管理员前往配置页面设置！");
            }
            return result;
        }

        // 周日（1）：只返回月末值班人员（如果是月末的话）
        return result;
    }

    /**
     * 判断指定日期是单周还是双周
     */
    /**
     * 判断指定日期是单周还是双周（基于自然的星期一为一周起点的规则）
     */
    private boolean isSingleWeek(Date targetDate, Date baseDate) {
        LocalDate target = targetDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate base = baseDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        // 统一将两个日期都对齐到各自所在周的“星期一”
        LocalDate targetMonday = target.with(DayOfWeek.MONDAY);
        LocalDate baseMonday = base.with(DayOfWeek.MONDAY);

        // 计算两个星期一之间相差的完整周数
        long diffWeeks = Math.abs(ChronoUnit.WEEKS.between(baseMonday, targetMonday));

        // 如果相差偶数周，说明属性相同（同为单周或同为双周）；奇数周则翻转
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

    /**
     * 计算值班天数
     * @param userId 用户ID
     * @param type 统计类型：month-本月, year-本年
     * @return
     */
    @Override
    public Integer getDutyCount(Long userId, String type) {
        ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(type == null || (!type.equals("month") && !type.equals("year")), ErrorCode.PARAMS_ERROR);

        // 1. 使用 Java 8 的 LocalDate 来处理日期边界（比 Calendar 好用一万倍）
        LocalDate today = LocalDate.now();
        LocalDate startDate;

        if ("month".equals(type)) {
            startDate = today.withDayOfMonth(1); // 本月 1 号
        } else {
            startDate = today.withDayOfYear(1);  // 本年 1 月 1 号
        }

        // 将 LocalDate 转换为 java.util.Date，以便传给 MyBatis-Plus 查询
        Date queryStartDate = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        // 获取今天零点，确保只统计“过去”的值班（如果你想把今天的也算进去，这里可以不截断时间）
        Date queryEndDate = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());

        // 2. 直接从 duty_record 真实记录表中 COUNT！
        QueryWrapper<DutyRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("userId", userId); // 注意：确保这里的字段名和实体类一致，如果是下划线请改为 user_id
        wrapper.eq("isDelete", 0);

        // 状态必须是正常值班（如果你的表里有请假 absent、替班 substitute 等状态，这里可以精准过滤）
        // wrapper.eq("status", "normal");

        // 统计范围：大于等于起始日，且严格小于今天（严格按照你原代码只算过去日期的逻辑）
        wrapper.ge("dutyDate", queryStartDate);
        wrapper.lt("dutyDate", queryEndDate);

        // 3. 返回真实的记录数
        long actualCount = dutyRecordMapper.selectCount(wrapper);

        return Math.toIntExact(actualCount);
    }

    /***
     * ============================================原本前辈选择的计算值班时间的逻辑
     * @param request 更新请求
     * @return
     */
//    @Override
//    public Integer getDutyCount(Long userId, String type) {
//        ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR);
//        ThrowUtils.throwIf(type == null || (!type.equals("month") && !type.equals("year")), ErrorCode.PARAMS_ERROR);
//
//        // 检查用户是否在值班配置中
//        QueryWrapper<DutyPerson> wrapper = new QueryWrapper<>();
//        wrapper.eq("userId", userId);
//        wrapper.eq("isDelete", 0);
//        List<DutyPerson> dutyPersonList = dutyPersonMapper.selectList(wrapper);
//
//        if (dutyPersonList == null || dutyPersonList.isEmpty()) {
//            return 0;
//        }
//
//        // 获取用户的值班类型
//        boolean isWeekdayDuty = dutyPersonList.stream().anyMatch(dp -> "weekday".equals(dp.getDutyType()));
//        boolean isSaturdayGroup1 = dutyPersonList.stream().anyMatch(dp -> "saturday_group1".equals(dp.getDutyType()));
//        boolean isSaturdayGroup2 = dutyPersonList.stream().anyMatch(dp -> "saturday_group2".equals(dp.getDutyType()));
//        boolean isMonthEndDuty = dutyPersonList.stream().anyMatch(dp -> "month_end".equals(dp.getDutyType()));
//
//        if (!isWeekdayDuty && !isSaturdayGroup1 && !isSaturdayGroup2 && !isMonthEndDuty) {
//            return 0;
//        }
//
//        // 获取基准日期
//        QueryWrapper<DutyConfig> configWrapper = new QueryWrapper<>();
//        configWrapper.eq("isDelete", 0);
//        configWrapper.orderByDesc("createTime");
//        configWrapper.last("LIMIT 1");
//        DutyConfig config = dutyConfigMapper.selectOne(configWrapper);
//        Date baseDate = config != null ? config.getBaseDate() : null;
//
//        // 计算时间范围
//        Calendar now = Calendar.getInstance();
//        int currentYear = now.get(Calendar.YEAR);
//        int currentMonth = now.get(Calendar.MONTH);
//
//        Calendar startDate = Calendar.getInstance();
//        Calendar endDate = Calendar.getInstance();
//
//        if ("month".equals(type)) {
//            // 本月
//            startDate.set(currentYear, currentMonth, 1, 0, 0, 0);
//            startDate.set(Calendar.MILLISECOND, 0);
//            endDate.set(currentYear, currentMonth, now.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59);
//            endDate.set(Calendar.MILLISECOND, 999);
//        } else {
//            // 本年
//            startDate.set(currentYear, 0, 1, 0, 0, 0);
//            startDate.set(Calendar.MILLISECOND, 0);
//            endDate.set(currentYear, 11, 31, 23, 59, 59);
//            endDate.set(Calendar.MILLISECOND, 999);
//        }
//
//        // 获取今天的日期（忽略时间部分）
//        Calendar today = Calendar.getInstance();
//        today.set(Calendar.HOUR_OF_DAY, 0);
//        today.set(Calendar.MINUTE, 0);
//        today.set(Calendar.SECOND, 0);
//        today.set(Calendar.MILLISECOND, 0);
//
//        // 计算值班天数（只统计已过去的日期）
//        int count = 0;
//        Calendar currentDate = (Calendar) startDate.clone();
//        currentDate.set(Calendar.HOUR_OF_DAY, 0);
//        currentDate.set(Calendar.MINUTE, 0);
//        currentDate.set(Calendar.SECOND, 0);
//        currentDate.set(Calendar.MILLISECOND, 0);
//
//        while (currentDate.before(endDate) || currentDate.equals(endDate)) {
//            // 只统计当前日期之前的日期（不包括今天）
//            if (currentDate.before(today)) {
//                int dayOfWeek = currentDate.get(Calendar.DAY_OF_WEEK); // 1-7, 1是周日
//                boolean counted = false;
//
//                // 检查是否为月末最后两天
//                boolean isMonthEndDay = isMonthEndDay(currentDate.getTime());
//
//                // 如果是月末且用户在月末值班列表中，则计数
//                if (isMonthEndDay && isMonthEndDuty) {
//                    count++;
//                    counted = true;
//                }
//
//                // 周一到周五（2-6）：如果用户在工作日值班列表中，则计数
//                if (dayOfWeek >= 2 && dayOfWeek <= 6 && isWeekdayDuty) {
//                    count++;
//                    counted = true;
//                }
//
//                // 周六（7）：根据单周/双周判断
//                if (!counted && dayOfWeek == 7) {
//                    if (baseDate != null) {
//                        boolean isSingle = isSingleWeek(currentDate.getTime(), baseDate);
//                        if ((isSingle && isSaturdayGroup1) || (!isSingle && isSaturdayGroup2)) {
//                            count++;
//                        }
//                    } else {
//                        // 如果没有基准日期，默认单周组
//                        if (isSaturdayGroup1) {
//                            count++;
//                        }
//                    }
//                }
//                // 周日（1）：不安排值班，不计数
//            }
//            // 移动到下一天
//            currentDate.add(Calendar.DAY_OF_MONTH, 1);
//        }
//
//        return count;
//    }
    ///  ==============================================END=============================

    @Override
    // todo 这个是干嘛的？
    @Transactional(rollbackFor = Exception.class)
    public List<DutyPersonVO> updateDutyConfigFrom(DutyConfigUpdateFromRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(request.getStartDate() == null || request.getStartDate().trim().isEmpty(), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(request.getConfig() == null || request.getConfig().isEmpty(), ErrorCode.PARAMS_ERROR);

        try {
            // ... 前面的时间解析逻辑保持不变 ...
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date startDate = sdf.parse(request.getStartDate());

            Calendar startCal = Calendar.getInstance();
            startCal.setTime(startDate);
            startCal.set(Calendar.HOUR_OF_DAY, 0);
            // ... 省略截断时间代码 ...
            Date actualStartDate = startCal.getTime();

            Calendar endCal = Calendar.getInstance();
            endCal.setTime(actualStartDate);
            endCal.add(Calendar.YEAR, 1);
            // ... 省略截断时间代码 ...
            Date actualEndDate = endCal.getTime();

            Map<String, Object> config = request.getConfig();
            syncDutyPersonsFromConfig(config);

            QueryWrapper<DutyConfig> configWrapper = new QueryWrapper<>();
            configWrapper.eq("isDelete", 0);
            configWrapper.orderByDesc("createTime");
            configWrapper.last("LIMIT 1");
            DutyConfig dutyConfig = dutyConfigMapper.selectOne(configWrapper);
            Date baseDate = dutyConfig != null ? dutyConfig.getBaseDate() : null;

            // 🌟 严格执行 SQL: UPDATE duty_record SET isDelete = 1 WHERE dutyDate > CURDATE();
            // ==========================================
            LambdaUpdateWrapper<DutyRecord> updateWrapper = new LambdaUpdateWrapper<>();

            // 使用 apply 直接调用 MySQL 的原生 CURDATE() 函数
            updateWrapper.apply("dutyDate > CURDATE()");
            // 排除临时排班
            updateWrapper.ne(DutyRecord::getDutyType, "groupe_temporaire");
            // SET isDelete = 1
            updateWrapper.set(DutyRecord::getIsDelete, 1);

            dutyRecordMapper.update(null, updateWrapper);


            // ==========================================

            // 第三步：遍历「指定日期及之后」的每一天
            Calendar currentCal = Calendar.getInstance();
            currentCal.setTime(actualStartDate);

            while (currentCal.compareTo(endCal) <= 0) {
                Date currentDate = currentCal.getTime();

                // 核心妙招：直接调用咱们刚修好的 getDutyPersonListByDate 方法！
                // 它内部已经完美处理好了：是不是工作日？是不是周末？单周还是双周？是不是月末？
                List<DutyPersonVO> personsForToday = getDutyPersonListByDate(currentDate);

                // 如果今天需要排班，就把他们一个个写进数据库
                if (personsForToday != null && !personsForToday.isEmpty()) {
                    for (DutyPersonVO person : personsForToday) {
                        // 真正的生成插入动作！
                        // 注意：这里传的是 person.getId()，也就是真实的 userId
                        createDutyRecord(person.getId(), currentDate, person.getDutyType(), "配置更新自动重排");
                    }
                }

                // 游标推进一步：进入下一天
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
     * 获取部门信息接口
     * @return
     */
    @Override
    public List<DeptVO> selectDeptVOList() {
        List<Duty> duties = dutyMapper.selectList(null);
        List<DeptVO> deptVOList = new ArrayList<>();
        for (Duty duty : duties) {
            DeptVO deptVO = new DeptVO();
            BeanUtils.copyProperties(duty, deptVO);
            deptVOList.add(deptVO);
        }
        return deptVOList;
    }

    @Override
    public Long addTemperateDutyPerson(DutyPersonTempareAddRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(request.getUserId() == null || request.getUserId() <= 0, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(request.getDutyType() == null || request.getDutyType().trim().isEmpty(), ErrorCode.PARAMS_ERROR);

        // 验证值班类型
        // 临时值班添加组
        String dutyType = request.getDutyType();
        if (!dutyType.equals("groupe_temporaire")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "并非临时分组");
        }

        // 检查用户是否存在
        User user = userMapper.selectById(request.getUserId());
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");

        // 创建值班人员对象
        DutyPerson dutyPerson = new DutyPerson();
        // 将请求体的用户信息赋值给对象
        BeanUtils.copyProperties(request, dutyPerson);
        // 检查是否已存在相同的配置
        dutyPerson = dutyMapper.selectPersonByIdAndIsdelete(dutyPerson);
        // 先判断是否为空
        // 拿过来的数据直接拿isDelete是否为0判断，如果是0就返回已存在，不为0为1就进行0修改
        if(dutyPerson!=null){
            switch (dutyPerson.getIsDelete()){
                // 已存在并未逻辑删除
                case 0: throw new BusinessException(ErrorCode.PARAMS_ERROR, "该用户已在此值班类型中");
                // 数据存在但是逻辑删除
                case 1: dutyPerson.setIsDelete(0);
                        // 修改逻辑删除状态,并判断是否操作成功
                        if(!dutyPersonMapper.updateByPersonIdIsDelete(dutyPerson)){
                            throw new BusinessException(ErrorCode.OPERATION_ERROR);
                        }
                        break;
                default:break;
            }
            // 数据表中并没有用户排班信息操作
        }else{
            // 赋值给对象准备插入表
            dutyPerson = new DutyPerson();
            dutyPerson.setUserId(request.getUserId());
            dutyPerson.setIsDelete(0);
            dutyPerson.setDutyType(request.getDutyType());
            try{
                // 插入表并判断是否操作成功
                ThrowUtils.throwIf(dutyPersonMapper.insert(dutyPerson) <= 0, ErrorCode.OPERATION_ERROR);
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        // 返回添加完成的排班人员id
        return dutyPerson.getId();
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

