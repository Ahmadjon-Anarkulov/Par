package com.parlament.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting service using Bucket4j.
 * Limits per user to prevent abuse.
 * Buckets are periodically cleaned up to prevent memory leaks.
 */
@Service
public class RateLimitService {

    private static final Logger log = LoggerFactory.getLogger(RateLimitService.class);

    // In production, use Redis-backed buckets
    private final ConcurrentHashMap<Long, Bucket> userBuckets = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, Long> lastAccessTime = new ConcurrentHashMap<>();

    // Allow 10 requests per minute per user
    private final Bandwidth limit = Bandwidth.classic(10, Refill.intervally(10, Duration.ofMinutes(1)));

    public boolean tryConsume(long userId) {
        lastAccessTime.put(userId, System.currentTimeMillis());
        Bucket bucket = userBuckets.computeIfAbsent(userId, id -> Bucket.builder().addLimit(limit).build());
        boolean allowed = bucket.tryConsume(1);
        if (!allowed) {
            log.warn("Rate limit exceeded for user {}", userId);
        }
        return allowed;
    }

    /**
     * Cleans up buckets for users who have been inactive for more than 10 minutes.
     * Prevents unbounded memory growth in long-running instances.
     */
    @Scheduled(fixedDelay = 600_000) // every 10 minutes
    public void cleanupInactiveBuckets() {
        long cutoff = System.currentTimeMillis() - Duration.ofMinutes(10).toMillis();
        int removed = 0;
        for (Map.Entry<Long, Long> entry : lastAccessTime.entrySet()) {
            if (entry.getValue() < cutoff) {
                userBuckets.remove(entry.getKey());
                lastAccessTime.remove(entry.getKey());
                removed++;
            }
        }
        if (removed > 0) {
            log.debug("Cleaned up {} inactive rate-limit buckets", removed);
        }
    }
}
