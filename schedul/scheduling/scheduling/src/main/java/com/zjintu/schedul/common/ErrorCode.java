package com.zjintu.schedul.common;

/**
 * 自定义错误码
 *
 
 */
public enum ErrorCode {

    SUCCESS(0, "ok"),
    PARAMS_ERROR(40000, "请求参数错误"),
    NOT_LOGIN_ERROR(40100, "未登录"),
    NO_AUTH_ERROR(40101, "无权限"),
    NOT_FOUND_ERROR(40400, "请求数据不存在"),
    FORBIDDEN_ERROR(40300, "禁止访问"),
    SYSTEM_ERROR(50000, "系统内部异常"),
    OPERATION_ERROR(50001, "操作失败"),


    /// ===================节假日类报错60000-60900
    EXIT_HOLIDAY_READY_ERROR(60000,"已经存在该日期,无需修改"),
    HOLIDAY_PARAMS_ERROR(60001,"错误的日期输入格式"),
    NO_DATE_DELETE_ERROR(60002,"删除日期不存在");




    /**
     * 状态码
     */
    private final int code;

    /**
     * 信息
     */
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
