package com.nun.aitestcase.config;

import com.nun.aitestcase.common.BusinessException;
import com.nun.aitestcase.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    public AuthInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String token = extractToken(request);
        if (token == null) {
            throw new BusinessException(401, "Authentication required");
        }
        if (!jwtUtil.validateToken(token)) {
            throw new BusinessException(401, "Invalid or expired token");
        }
        Claims claims = jwtUtil.parseToken(token);
        Object userIdObj = claims.get("userId");
        Long userId = (userIdObj instanceof Integer) ? ((Integer) userIdObj).longValue() : (Long) userIdObj;
        request.setAttribute("userId", userId);
        request.setAttribute("username", claims.getSubject());
        request.setAttribute("role", claims.get("role"));
        return true;
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
