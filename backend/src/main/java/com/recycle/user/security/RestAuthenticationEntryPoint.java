package com.recycle.user.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recycle.common.api.R;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 未登录访问受保护接口 → 401 + 统一响应体，而不是 Spring 默认的登录页重定向。
 */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    /** 业务码：未登录 */
    public static final int UNAUTHORIZED = 40100;

    private final ObjectMapper objectMapper;

    public RestAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(
                R.fail(UNAUTHORIZED, "请先登录")));
    }
}
