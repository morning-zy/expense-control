package com.maning.expensecontrol.common;

import com.maning.expensecontrol.exception.BusinessException;
import com.maning.expensecontrol.exception.ErrorCode;

/**
 * 当前登录用户（一次请求内有效）。
 * <p>
 * 用 ThreadLocal 存：Tomcat 一个线程处理一次请求，请求结束必须 remove()，
 * 否则线程被复用时会串到下一个用户身上（这叫线程池污染，线上排查起来很痛苦）。
 * 清理放在拦截器的 afterCompletion 里，无论正常返回还是抛异常都会执行。
 */
public final class UserContext {

    private static final ThreadLocal<Long> CURRENT_USER_ID = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(Long userId) {
        CURRENT_USER_ID.set(userId);
    }

    public static Long getUserId() {
        return CURRENT_USER_ID.get();
    }

    /** 取不到就抛未登录，避免业务代码里出现 null 判断满天飞 */
    public static Long requireUserId() {
        Long userId = CURRENT_USER_ID.get();
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return userId;
    }

    public static void clear() {
        CURRENT_USER_ID.remove();
    }
}
