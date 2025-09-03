package com.cdcrane.social_konnect_backend.config.filter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * This rate limiter filter works perfectly fine when the application is receiving requests directly from the client.
 * If there is a reverse proxy between the client and server, you must get the IP from the X-Forwarded-For header instead.
 */
@Component
public class RateLimiterFilter implements Filter {

    // Caffeine cache with eviction of inactive IPs
    private final Cache<String, Bucket> cache = Caffeine.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES) // auto-evict if no requests for 10 minutes
            .build();

    private Bucket createNewBucket() {
        return Bucket.builder()
                .addLimit(limit -> limit.capacity(20).refillGreedy(5, Duration.ofSeconds(1)))    // short-term
                .addLimit(limit -> limit.capacity(300).refillGreedy(300, Duration.ofMinutes(5))) // long-term
                .build();
    }

    // Lookup or create bucket per client IP
    private Bucket resolveBucket(String ip) {
        return cache.get(ip, k -> createNewBucket());
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // Get the path.
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String path = httpRequest.getRequestURI();

        // Exclude on media since there is no logic, only static files.
        if(path.startsWith("/media/")){
            chain.doFilter(request, response);
            return;
        }

        // Get the client IP and check if there is already a bucket for them in the cache.
        String clientIp = ((HttpServletRequest) request).getRemoteAddr();
        Bucket bucket = resolveBucket(clientIp);

        // Check if they are currently rate-limited.
        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
        } else {
            HttpServletResponse httpResp = (HttpServletResponse) response;
            httpResp.setStatus(429);
            httpResp.getWriter().write("Too many requests from your IP. Please slow down.");
        }
    }
}
