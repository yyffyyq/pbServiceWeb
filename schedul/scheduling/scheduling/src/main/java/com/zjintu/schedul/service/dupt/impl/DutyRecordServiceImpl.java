package com.zjintu.schedul.service.dupt.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zjintu.schedul.common.ErrorCode;
import com.zjintu.schedul.exception.BusinessException;
import com.zjintu.schedul.exception.ThrowUtils;
import com.zjintu.schedul.mapper.duty.DeptToUserMapper;
import com.zjintu.schedul.mapper.duty.DutyRecordMapper;
import com.zjintu.schedul.mapper.user.UserMapper;
import com.zjintu.schedul.model.dto.duty.DutyRecordTempareRequest;
import com.zjintu.schedul.model.entity.dupt.DutyRecord;
import com.zjintu.schedul.model.entity.user.User;
import com.zjintu.schedul.model.vo.duptVO.DutyPersonVO;
import com.zjintu.schedul.model.vo.duptVO.DutyRecordTempareVO;
import com.zjintu.schedul.model.vo.duptVO.DutyRecordVO;
import com.zjintu.schedul.service.dupt.DutyRecordService;
import com.zjintu.schedul.service.dupt.DutyService;
import com.zjintu.schedul.utils.ClearDateUtils;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
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

    @Resource
    private UserMapper userMapper;

    @Resource
    private DutyRecordMapper dutyRecordMapper;

    @Resource
    private DeptToUserMapper deptToUserMapper;


    @Override
    @Transactional(rollbackFor = Exception.class)
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
        return generateDutyRecordsForDate(date, null);
    }

    private Integer generateDutyRecordsForDate(Date date, String remark) {
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
                record.setRemark(remark); // 设置备注
                this.save(record);
                count++;
            }
        }

        return count;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer generateDutyRecordsForDateRange(Date startDate, Date endDate) {
        return generateDutyRecordsForDateRange(startDate, endDate, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer generateDutyRecordsForDateRange(Date startDate, Date endDate, String remark) {
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
            int count = generateDutyRecordsForDate(currentDate, remark);
            totalCount += count;
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        return totalCount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer updateFutureDutyRecords(String remark) {
        // 获取今天的日期（从今天开始更新）
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        Date todayDate = today.getTime();

        // 删除今天及以后的所有值班记录
        QueryWrapper<DutyRecord> deleteWrapper = new QueryWrapper<>();
        deleteWrapper.ge("dutyDate", todayDate);
        deleteWrapper.eq("isDelete", 0);
        this.remove(deleteWrapper);

        // 重新生成未来一个月的值班记录（根据新的配置）
        Calendar endDate = Calendar.getInstance();
        endDate.setTime(todayDate);
        endDate.add(Calendar.MONTH, 1);

        return generateDutyRecordsForDateRange(todayDate, endDate.getTime(), remark);
    }

    @Override
    public List<DutyRecordVO> getDutyRecordsByDateRange(Date startDate, Date endDate) {
        if (startDate == null || endDate == null) {
            return new ArrayList<>();
        }

        // 设置时间为0点
        Calendar startCal = Calendar.getInstance();
        startCal.setTime(startDate);
        startCal.set(Calendar.HOUR_OF_DAY, 0);
        startCal.set(Calendar.MINUTE, 0);
        startCal.set(Calendar.SECOND, 0);
        startCal.set(Calendar.MILLISECOND, 0);

        Calendar endCal = Calendar.getInstance();
        endCal.setTime(endDate);
        endCal.set(Calendar.HOUR_OF_DAY, 23);
        endCal.set(Calendar.MINUTE, 59);
        endCal.set(Calendar.SECOND, 59);
        endCal.set(Calendar.MILLISECOND, 999);

        // 查询指定日期范围内的值班记录
        QueryWrapper<DutyRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.ge("dutyDate", startCal.getTime());
        queryWrapper.le("dutyDate", endCal.getTime());
        queryWrapper.eq("isDelete", 0);
        queryWrapper.orderByAsc("dutyDate");

        List<DutyRecord> records = this.list(queryWrapper);
        
        return records.stream().map(record -> {
            DutyRecordVO vo = new DutyRecordVO();
            BeanUtils.copyProperties(record, vo);
            // 填充用户信息
            User user = userMapper.selectById(record.getUserId());
            if (user != null) {
                vo.setUserName(user.getUserName());
                vo.setDept(user.getDept());
                vo.setPhone(user.getPhone());
            }
            return vo;
        }).collect(Collectors.toList());
    }
/// ===================================临时值班组相关业务逻辑======================================
    /**
     * @param currentDate 插入临时组排班日期
     */
    @Override
    public String updateTheDayDutyRecord(Date currentDate,Long userId,String type_group) {
        // 判断参数
        ThrowUtils.throwIf(currentDate==null||userId == null, ErrorCode.PARAMS_ERROR);
        // 这里需要做一个用户id是否存在的判断
        if(userMapper.selectById(userId)==null){
            return "用户不存在";
        }
        // todo 需要添加判断，判断该用户是否在这一天值班，逻辑删除是否删除，分别用三套方案
        // 根据日期以及用户id去判断是否存在这天排班，该用户
        DutyRecord record = new DutyRecord();
//        record.setDutyDate(atStartOfDay(currentDate));
        record.setDutyDate(currentDate);
        record.setUserId(userId);
        record.setDutyType(type_group);
        record = dutyRecordMapper.selectDutyRecord(record);
        String result = "";
        if (record != null) {
            switch (record.getIsDelete()){
                case 0:result = "已存在";break;
                case 1:ThrowUtils.throwIf(!dutyRecordMapper.updateIsdelete(record), ErrorCode.OPERATION_ERROR);
                result = "添加成功";
                break;
                default:break;
            }
        }else {
            record = new DutyRecord();
            record.setUserId(userId);
            record.setDutyType(type_group);
            record.setDutyDate(atStartOfDay(currentDate));
            dutyRecordMapper.insert(record);
            result = "添加成功";
            return result;
        }
        return result;
    }

    /**
     * 通过部门编号、查询时间确定返回临时值班组人员
     * @param deptId 部门编号
     * @param dutyRecordTempareRequest 请求日期
     * @return
     */
    @Override
    public List<DutyRecordTempareVO> selectUserDuptTypeBydeptIdAndDuptDate(Long deptId, DutyRecordTempareRequest dutyRecordTempareRequest) {
        // 先获取需要回显的日期
        Date currentDate = ClearDateUtils.atStartOfDay(dutyRecordTempareRequest.getDutyDate());
        // 拿部门编号去查询用户编号
        List<Long> userIds = deptToUserMapper.selectUserIdByDuptId(deptId);
        if(userIds==null){
            return new ArrayList<>();
        }
        // 创建回显对象，放入用户编号、值班组名、用户名字
        List<DutyRecordTempareVO> vos = new ArrayList<>();
        // 那这用户编号和需要回显日期获取到这一天的人员值班组
        // 这里需要做一个判断，判断该日这个userId是否有值班
        for(Long userId:userIds){
            DutyRecordTempareVO vo = new DutyRecordTempareVO();
            String dutyType = dutyRecordMapper.selectByUserIdAndDutyDate(userId,currentDate);
            if(dutyType!=null){
                String userName = userMapper.selectById(userId).getUserName();
                vo.setUserId(userId);
                vo.setDutyType(dutyType);
                vo.setUserName(userName);
                vos.add(vo);
            }
        }
        // 封装返回信息
        return vos;
    }

    /**
     * 获取临时组用户编号列表
     * @param currentDate 查看日期
     * @param typeGroup 查看分组
     * @return 用户编号列表
     */
    @Override
    public List<Long> selectUserIdByGroupAndDate(Date currentDate, String typeGroup) {
        List<Long> userIds = dutyRecordMapper.selectUserIdList(currentDate,typeGroup);
        return userIds;
    }


    /**
     * 删除临时组值班人员信息
     * @param currentDate 插入临时组排班日期
     * @param deleteUserId 增加记录人员id
     * @param typeGroup 修改分组
     * @return
     */
    @Override
    public String deleteTheDayDutyRecord(Date currentDate, Long deleteUserId, String typeGroup) {
        // 判断传入值
        if(userMapper.selectById(deleteUserId) == null){
            return deleteUserId+"用户不存在";
        }
        Long result = dutyRecordMapper.deleteUserIdById(deleteUserId,typeGroup,currentDate);

        //
        if(result==0){
            return "删除失败";
        }
        return "删除成功";
    }

    /**
     * 将 Date 的时分秒毫秒全部清零，只保留日期部分
     */
    private Date atStartOfDay(Date date) {
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




