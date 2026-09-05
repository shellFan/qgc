package com.qiongguichou.admin.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qiongguichou.common.result.ErrorCode;
import com.qiongguichou.common.result.Result;
import com.qiongguichou.core.config.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 管理员认证拦截器
 * 验证Bearer Token中的管理员身份，与用户认证拦截器分开
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminAuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行OPTIONS预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            writeUnauthorized(response, ErrorCode.ADMIN_UNAUTHORIZED, "未登录");
            return false;
        }

        String token = authHeader.substring(7);
        try {
            // 验证JWT Token
            if (!jwtUtil.validateToken(token)) {
                writeUnauthorized(response, ErrorCode.ADMIN_UNAUTHORIZED, "Token已过期");
                return false;
            }

            // 检查是否为管理员Token
            String type = jwtUtil.getTokenType(token);
            if (!"admin".equals(type)) {
                writeUnauthorized(response, ErrorCode.ADMIN_FORBIDDEN, "非管理员Token");
                return false;
            }

            Long adminId = jwtUtil.getAdminId(token);
            request.setAttribute("adminId", adminId);
            return true;
        } catch (Exception e) {
            log.warn("管理员Token验证失败: {}", e.getMessage());
            writeUnauthorized(response, ErrorCode.ADMIN_UNAUTHORIZED, "Token验证失败");
            return false;
        }
    }

    private void writeUnauthorized(HttpServletResponse response, ErrorCode errorCode, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        Result<?> result = Result.error(errorCode, message);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}