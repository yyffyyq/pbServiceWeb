# 值班管理接口文档

## 接口列表

### 1. 值班配置相关接口

#### 1.1 获取值班配置
- **接口路径**: `GET /duty/config`
- **请求方式**: GET
- **权限要求**: 所有用户
- **功能说明**: 获取完整的值班配置（基准日期 + 所有值班人员列表）
- **返回数据**: 
```json
{
  "code": 0,
  "data": {
    "baseDate": "2024-01-01",
    "weekdayDutyList": [
      {
        "id": 1,
        "userName": "张三",
        "dept": "技术部",
        "phone": "13800138000",
        "dutyType": "weekday"
      }
    ],
    "saturdayGroup1": [...],
    "saturdayGroup2": [...],
    "monthEndDutyList": [...]
  },
  "message": ""
}
```

#### 1.2 保存/更新基准日期
- **接口路径**: `POST /duty/config/base-date`
- **请求方式**: POST
- **权限要求**: 仅管理员
- **请求参数**:
```json
{
  "baseDate": "2024-01-01"
}
```
- **返回数据**: 
```json
{
  "code": 0,
  "data": true,
  "message": ""
}
```

### 2. 值班人员管理接口

#### 2.1 添加值班人员
- **接口路径**: `POST /duty/person/add`
- **请求方式**: POST
- **权限要求**: 仅管理员
- **请求参数**:
```json
{
  "userId": 1,
  "dutyType": "weekday"  // weekday | saturday_group1 | saturday_group2 | month_end
}
```
- **返回数据**: 
```json
{
  "code": 0,
  "data": 123,  // 返回duty_person表的id
  "message": ""
}
```

#### 2.2 删除值班人员
- **接口路径**: `POST /duty/person/delete`
- **请求方式**: POST
- **权限要求**: 仅管理员
- **请求参数**:
```json
{
  "id": 123  // duty_person表的id
}
```
- **返回数据**: 
```json
{
  "code": 0,
  "data": true,
  "message": ""
}
```

#### 2.3 根据值班类型查询值班人员列表
- **接口路径**: `POST /duty/person/list/type`
- **请求方式**: POST
- **权限要求**: 仅管理员
- **请求参数**:
```json
{
  "dutyType": "weekday"  // weekday | saturday_group1 | saturday_group2 | month_end
}
```
- **返回数据**: 
```json
{
  "code": 0,
  "data": [
    {
      "id": 1,
      "userName": "张三",
      "dept": "技术部",
      "phone": "13800138000",
      "dutyType": "weekday"
    }
  ],
  "message": ""
}
```

#### 2.4 根据日期查询当天的值班人员列表（用于日历显示）
- **接口路径**: `POST /duty/person/list/date`
- **请求方式**: POST
- **权限要求**: 所有用户
- **请求参数**:
```json
{
  "date": "2024-01-15"  // 日期格式：yyyy-MM-dd
}
```
- **返回数据**: 
```json
{
  "code": 0,
  "data": [
    {
      "id": 1,
      "userName": "张三",
      "dept": "技术部",
      "phone": "13800138000",
      "dutyType": "weekday"
    }
  ],
  "message": ""
}
```
- **功能说明**: 
  - 自动判断日期类型（月末最后两天/工作日/周六/周日）
  - 如果是周六，会根据基准日期判断单周/双周
  - 返回对应类型的值班人员列表

### 3. 值班天数统计接口

#### 3.1 统计用户的值班天数
- **接口路径**: `POST /duty/count`
- **请求方式**: POST
- **权限要求**: 所有用户
- **请求参数**:
```json
{
  "userId": 1,
  "type": "month"  // month-本月, year-本年
}
```
- **返回数据**: 
```json
{
  "code": 0,
  "data": 15,  // 值班天数
  "message": ""
}
```
- **功能说明**: 
  - 只统计已过去的日期（不包括今天）
  - 根据用户的值班配置类型（工作日/周六单周组/周六双周组/月末）计算
  - 支持本月和本年统计

## 值班类型说明

- `weekday`: 工作日值班（周一到周五固定值班）
- `saturday_group1`: 周六单周组
- `saturday_group2`: 周六双周组
- `month_end`: 月末值班（每月最后两天）

## 数据库表结构

### duty_config 表
- 存储全局配置（基准日期）

### duty_person 表
- 存储值班人员配置
- 一个用户可以在多个值班类型中

### duty_record 表（可选）
- 记录实际值班情况
- 用于考勤和实际统计

