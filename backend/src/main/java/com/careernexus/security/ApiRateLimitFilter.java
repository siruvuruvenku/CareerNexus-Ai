package com.careernexus.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ApiRateLimitFilter extends OncePerRequestFilter {
    private final StringRedisTemplate redis;
    private final ConcurrentHashMap<String, Window> fallback = new ConcurrentHashMap<>();
    public ApiRateLimitFilter(StringRedisTemplate redis) { this.redis = redis; }

    @Override protected boolean shouldNotFilter(HttpServletRequest request) { return !request.getRequestURI().startsWith("/api/"); }

    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        long minute = System.currentTimeMillis() / 60000;
        String ip = request.getRemoteAddr();
        boolean auth = request.getRequestURI().startsWith("/api/auth/");
        String key = "rate:" + (auth ? "auth:" : "api:") + ip + ":" + minute;
        int limit = auth ? 12 : 180;
        long count;
        try {
            Long value = redis.opsForValue().increment(key);
            count = value == null ? 1 : value;
            if (count == 1) redis.expire(key, Duration.ofMinutes(2));
        } catch (DataAccessException ex) {
            Window window = fallback.compute(key, (k, old) -> old == null || old.minute() != minute ? new Window(minute, 1) : new Window(minute, old.count() + 1));
            count = window.count();
            if (fallback.size() > 5000) fallback.entrySet().removeIf(entry -> entry.getValue().minute() < minute - 2);
        }
        if (count > limit) {
            response.setStatus(429); response.setContentType("application/json"); response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"success\":false,\"data\":null,\"message\":\"Rate limit exceeded. Try again in a minute.\"}");
            return;
        }
        chain.doFilter(request, response);
    }
    private record Window(long minute, long count) {}
}

