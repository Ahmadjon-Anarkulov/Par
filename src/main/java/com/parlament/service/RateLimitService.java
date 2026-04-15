package com.parlament.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting service using Bucket4j.
 * Limits per user to prevent abuse.
 */
@Service
public class RateLimitService {

    private static final Logger log = LoggerFactory.getLogger(RateLimitService.class);

    // In production, use Redis-backed buckets
    private final ConcurrentHashMap<Long, Bucket> userBuckets = new ConcurrentHashMap<>();

    // Allow 10 requests per minute per user
    private final Bandwidth limit = Bandwidth.classic(10, Refill.intervally(10, Duration.ofMinutes(1)));

    public boolean tryConsume(long userId) {
        Bucket bucket = userBuckets.computeIfAbsent(userId, id -> Bucket.builder().addLimit(limit).build());
        boolean allowed = bucket.tryConsume(1);
        if (!allowed) {
            log.warn("Rate limit exceeded for user {}", userId);
        }
        return allowed;
    }
}