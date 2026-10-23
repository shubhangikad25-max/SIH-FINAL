package gov.aicte.apaar.security;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCKOUT_DURATION_MS = 15 * 60 * 1000; // 15 minutes

    private static class Attempt {
        int count;
        long lastAttemptTime;
        long lockedUntil;

        Attempt(long now) {
            this.count = 1;
            this.lastAttemptTime = now;
            this.lockedUntil = 0;
        }
    }

    private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();

    public boolean isBlocked(String key) {
        Attempt attempt = attempts.get(key);
        if (attempt == null) {
            return false;
        }
        long now = System.currentTimeMillis();
        if (attempt.lockedUntil > now) {
            return true;
        }
        if (attempt.lockedUntil > 0 && attempt.lockedUntil <= now) {
            // Lockout expired, reset
            attempts.remove(key);
            return false;
        }
        return false;
    }

    public void loginFailed(String key) {
        long now = System.currentTimeMillis();
        attempts.compute(key, (k, attempt) -> {
            if (attempt == null) {
                return new Attempt(now);
            }
            attempt.count++;
            attempt.lastAttemptTime = now;
            if (attempt.count >= MAX_FAILED_ATTEMPTS) {
                attempt.lockedUntil = now + LOCKOUT_DURATION_MS;
            }
            return attempt;
        });
    }

    public void loginSucceeded(String key) {
        attempts.remove(key);
    }
}
