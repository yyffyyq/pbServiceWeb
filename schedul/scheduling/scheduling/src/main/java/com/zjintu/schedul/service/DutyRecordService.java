package com.zjintu.schedul.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.zjintu.schedul.model.entity.DutyRecord;
import com.zjintu.schedul.model.vo.DutyRecordVO;

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
}
