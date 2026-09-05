package com.qiongguichou.config;

import com.qiongguichou.core.interceptor.UserAuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebMvc配置 - 注册用户认证拦截器
 * 管理员拦截器由AdminWebMvcConfig单独注册
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private UserAuthInterceptor userAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
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
                        // Swagger
                        "/swagger-resources/**",
                        "/webjars/**",
                        "/v2/api-docs",
                        "/swagger-ui.html/**"
                );
    }
}