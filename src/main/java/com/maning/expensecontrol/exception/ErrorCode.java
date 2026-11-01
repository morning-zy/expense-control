package com.maning.expensecontrol.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    SUCCESS(0, "success"),

    PARAM_INVALID(1001, "参数校验失败"),
    NOT_FOUND(1002, "资源不存在"),

    EMP_NO_DUPLICATE(2001, "工号已存在"),
    DEPT_NOT_FOUND(2002, "所属部门不存在"),

    INTERNAL_ERROR(5000, "服务器内部错误"),

    CATEGORY_NOT_FOUND(2003, "费用科目不存在或已停用"),
    EMPLOYEE_NOT_FOUND(2004, "员工不存在"),
    ITEM_AMOUNT_EXCEED_LIMIT(2005, "单笔金额超过科目限额"),

    // ===== 3-4 审批状态机 =====
    FORM_STATUS_INVALID(2006, "单据当前状态不允许该操作"),
    NO_APPROVER_CONFIGURED(2007, "未配置审批人，请联系管理员"),
    NOT_CURRENT_APPROVER(2008, "你不是当前节点的审批人");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
