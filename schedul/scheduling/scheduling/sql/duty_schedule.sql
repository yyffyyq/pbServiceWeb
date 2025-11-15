-- 值日安排表
-- 用于存储值班配置信息

-- 值班配置表（存储全局配置，如基准日期）
create table if not exists duty_config
(
    id         bigint auto_increment comment 'id' primary key,
    baseDate  date                               null comment '基准日期（用于判断单周/双周，该日期所在周为单周）',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除'
) comment '值班配置表' collate = utf8mb4_unicode_ci;

-- 值班人员表（存储具体的值班人员配置）
create table if not exists duty_person
(
    id         bigint auto_increment comment 'id' primary key,
    userId    bigint                             not null comment '用户ID（关联sys_user表）',
    dutyType  varchar(50)                        not null comment '值班类型：weekday-工作日值班, saturday_group1-周六单周组, saturday_group2-周六双周组, month_end-月末值班',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isTelete   tinyint  default 0                 not null comment '是否删除',
    index idx_user_id (userTd),
    index idx_duty_type (dutyType),
    index idx_create_time (createTime)
) comment '值班人员表' collate = utf8mb4_unicode_ci;

-- 值班记录表（记录实际的值班情况，用于统计和考勤）
create table if not exists duty_record
(
    id         bigint auto_increment comment 'id' primary key,
    userId    bigint                             not null comment '用户ID（关联sys_user表）',
    dutyDate  date                               not null comment '值班日期',
    dutyType  varchar(50)                        not null comment '值班类型：weekday-工作日值班, saturday_group1-周六单周组, saturday_group2-周六双周组, month_end-月末值班',
    status     varchar(20)  default 'normal'      not null comment '值班状态：normal-正常, absent-缺勤, substitute-代班',
    remark     varchar(500)                        null comment '备注（如代班人员、缺勤原因等）',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除',
    index idx_user_id (userId),
    index idx_duty_date (dutyDate),
    index idx_duty_type (dutyType),
    index idx_create_time (createTime),
    unique key uk_user_date (userId, dutyDate, isDelete) comment '同一用户同一天只能有一条记录'
) comment '值班记录表' collate = utf8mb4_unicode_ci;

