package com.zjintu.schedul.service;

import com.zjintu.schedul.model.dto.duty.*;
import com.zjintu.schedul.model.vo.duptVO.DeptVO;
import com.zjintu.schedul.model.vo.duptVO.DutyConfigVO;
import com.zjintu.schedul.model.vo.duptVO.DutyPersonVO;

import java.util.Date;
import java.util.List;

/**
 * 值班服务接口
 */
public interface DutyService {
    /**
     * 获取值班配置（包括基准日期和所有值班人员列表）
     */
    DutyConfigVO getDutyConfig();

    /**
     * 保存/更新基准日期
     */
    Boolean saveBaseDate(DutyConfigRequest request);

    /**
     * 添加值班人员
     */
    Long addDutyPerson(DutyPersonAddRequest request);

    /**
     * 删除值班人员
     */
    Boolean deleteDutyPerson(Long id);

    /**
     * 根据值班类型查询值班人员列表
     */
    List<DutyPersonVO> getDutyPersonListByType(String dutyType);

    /**
     * 根据日期查询当天的值班人员列表
     */
    List<DutyPersonVO> getDutyPersonListByDate(Date date);

    /**
     * 统计用户的值班天数
     * @param userId 用户ID
     * @param type 统计类型：month-本月, year-本年
     * @return 值班天数
     */
    Integer getDutyCount(Long userId, String type);

    /**
     * 从指定日期开始更新值班配置
     * @param request 更新请求
     * @return 更新后开始日期的值班人员列表
     */
    List<DutyPersonVO> updateDutyConfigFrom(DutyConfigUpdateFromRequest request);

    /**
     * 获取部门信息接口
     * @return List<DeptVO>
     */
    List<DeptVO> selectDeptVOList();

}

