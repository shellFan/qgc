package com.qiongguichou.config;

import com.qiongguichou.core.interceptor.UserAuthInterceptor;
import com.qiongguichou.interceptor.RateLimitInterceptor;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class WebMvcConfigTest {

    @Test
    void exposesCommentListWithoutUserAuthentication() throws Exception {
        WebMvcConfig config = new WebMvcConfig();
        ReflectionTestUtils.setField(config, "userAuthInterceptor", mock(UserAuthInterceptor.class));
        ReflectionTestUtils.setField(config, "rateLimitInterceptor", mock(RateLimitInterceptor.class));

        InterceptorRegistry registry = new InterceptorRegistry();
        config.addInterceptors(registry);

        Field registrationsField = InterceptorRegistry.class.getDeclaredField("registrations");
        registrationsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<InterceptorRegistration> registrations =
                (List<InterceptorRegistration>) registrationsField.get(registry);
        InterceptorRegistration authRegistration = registrations.get(1);

        @SuppressWarnings("unchecked")
        List<String> excludes = (List<String>) ReflectionTestUtils.getField(authRegistration, "excludePatterns");
        assertTrue(excludes.contains("/api/comment/list/**"));
    }
}
