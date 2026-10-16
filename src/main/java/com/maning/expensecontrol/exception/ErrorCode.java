package com.maning.expensecontrol.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    SUCCESS(0, "success"),

    PARAM_INVALID(1001, "参数校验失败"),
    NOT_FOUND(1002, "资源不存在"),

    EMP_NO_DUPLICATE(2001, "工号已存在"),
    DEPT_NOT_FOUND(2002, "所属部门不存在"),

    INTERNAL_ERROR(5000, "服务器内部错误");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
