package com.example.devSns.interceptor;

import com.example.devSns.dto.GenericPairDto;
import com.example.devSns.exception.NotFoundException;
import com.example.devSns.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


@Slf4j
@Component
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final Map<String, Set<HttpMethod>> allowedMethods;


    public JwtInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
        this.allowedMethods = new HashMap<>();
    }

    public void setMethodAllows(String url, Set<HttpMethod> allowedHttpMethods) {
        allowedMethods.put(url, allowedHttpMethods);
    }

    private boolean checkMethodAllows(String url, HttpMethod httpMethod) {
        return allowedMethods.containsKey(url) && allowedMethods.get(url).contains(httpMethod);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (CorsUtils.isPreFlightRequest(request)) {
            return true;
        }
        String pattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);

        if (pattern == null) {
            return false;
        }

        if (checkMethodAllows(pattern, HttpMethod.valueOf(request.getMethod()))) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        log.info("Authorization header: {}", authHeader);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized");
            return false;
        }

        // "Bearer " 제거
        String token = authHeader.substring(7);

        if (!jwtUtil.validateToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized");
            return false;
        }

        Long userId = jwtUtil.getUserIdFromToken(token);
        request.setAttribute("userId", userId);

        return true;
    }

}
