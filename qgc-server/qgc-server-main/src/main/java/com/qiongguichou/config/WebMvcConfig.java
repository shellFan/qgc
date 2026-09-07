package com.qiongguichou.config;

import com.qiongguichou.core.interceptor.UserAuthInterceptor;
import com.qiongguichou.interceptor.RateLimitInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebMvc配置 - 注册用户认证拦截器+限流拦截器
 * 管理员拦截器由AdminWebMvcConfig单独注册
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private UserAuthInterceptor userAuthInterceptor;

    @Autowired
    private RateLimitInterceptor rateLimitInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 限流拦截器(优先级最高，所有API都限流)
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/api/**")
                .order(0);

        // 用户认证拦截器
        registry.addInterceptor(userAuthInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        // 认证相关
                        "/api/user/auth/**",
                        "/api/wechat/**",
                        // 开发模式
                        "/dev/**",
                        // 公开接口
                        "/api/campaign/list",
                        "/api/campaign/detail/**",
                        "/api/campaign/hot",
                        "/api/campaign/category/list",
                        "/api/proof/detail/**",
                        // 健康检查和版本
                        "/api/health",
                        "/api/health/live",
                        "/api/health/ready",
                        "/api/version",
                        // Swagger
                        "/swagger-resources/**",
                        "/webjars/**",
                        "/v2/api-docs",
                        "/swagger-ui.html/**"
                )
                .order(1);
    }
}