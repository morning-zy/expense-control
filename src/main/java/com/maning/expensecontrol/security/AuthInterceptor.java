package com.maning.expensecontrol.security;

import com.maning.expensecontrol.common.UserContext;
import com.maning.expensecontrol.exception.BusinessException;
import com.maning.expensecontrol.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录拦截：校验 Authorization: Bearer &lt;token&gt;，通过后把用户 id 放进 UserContext。
 * <p>
 * 抛出的 BusinessException 会被 GlobalExceptionHandler 接住，
 * 返回 {"code":401,...}，前端拿这个统一跳登录页。
 */
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private static final String HEADER = "Authorization";
    private static final String PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 预检请求不带 token，必须放行，否则浏览器跨域直接失败
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String header = request.getHeader(HEADER);
        if (header == null || !header.startsWith(PREFIX)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "缺少登录令牌，请先登录");
        }
        Long userId = jwtUtil.parseUserId(header.substring(PREFIX.length()).trim());
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "登录已过期或令牌无效，请重新登录");
        }
        UserContext.set(userId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        // 线程复用前必须清掉，否则下一个请求会读到上一个用户的 id
        UserContext.clear();
    }
}
