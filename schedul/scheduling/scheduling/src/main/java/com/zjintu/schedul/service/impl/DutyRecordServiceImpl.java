package com.zjintu.schedul.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zjintu.schedul.mapper.DutyRecordMapper;
import com.zjintu.schedul.model.entity.DutyRecord;
import com.zjintu.schedul.model.vo.DutyRecordVO;
import com.zjintu.schedul.service.DutyRecordService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

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

    @Override
    public List<DutyRecordVO> getDutyRecordsByUserId(Long userId) {
        QueryWrapper<DutyRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.orderByDesc("duty_date");
        List<DutyRecord> records = this.list(queryWrapper);
        
        return records.stream().map(record -> {
            DutyRecordVO vo = new DutyRecordVO();
            BeanUtils.copyProperties(record, vo);
            return vo;
        }).collect(Collectors.toList());
    }
}




