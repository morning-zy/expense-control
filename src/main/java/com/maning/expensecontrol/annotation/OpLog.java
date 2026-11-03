package com.maning.expensecontrol.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作留痕标记：标在 Service 方法上，由 OperationLogAspect 自动写 operation_log。
 * <p>
 * 为什么不标在 Controller：Controller 拿不到「业务主键」和「变更前后值」，
 * 标在 Service 上切面能拿到入参、返回值和异常，记录才有内容。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OpLog {

    /** 业务类型：EXPENSE_FORM / BUDGET / EMPLOYEE */
    String bizType();

    /** 动作：CREATE / SUBMIT / APPROVE / REJECT / UPDATE / CANCEL */
    String action();

    /** 从第几个入参取业务主键，默认第 0 个；没有主键时填 -1 */
    int bizIdArgIndex() default 0;
}
