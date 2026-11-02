package com.maning.expensecontrol.config;

import com.maning.expensecontrol.security.AuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 注册登录拦截器。
 * <p>
 * 只拦 /api/**，放行登录接口和健康检查 —— 登录接口本身要求 token 就成死循环了。
 * 后续要加白名单（比如注册、验证码），也在这里加 excludePathPatterns。
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/login",
                        "/api/ping"
                );
    }
}
