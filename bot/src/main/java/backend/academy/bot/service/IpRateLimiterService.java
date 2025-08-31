package backend.academy.bot.service;

import backend.academy.bot.config.ApplicationConfig;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IpRateLimiterService {
    private final RateLimiterRegistry registry;
    private final ConcurrentMap<String, RateLimiter> ipRateLimiters = new ConcurrentHashMap<>();

    @Autowired
    public IpRateLimiterService(ApplicationConfig config) {
        final RateLimiterConfig rateLimiterConfig = RateLimiterConfig.custom()
                .timeoutDuration(config.rateLimiter().timeoutDuration())
                .limitForPeriod(config.rateLimiter().limitForPeriod())
                .limitRefreshPeriod(config.rateLimiter().limitRefreshPeriod())
                .build();

        registry = RateLimiterRegistry.of(rateLimiterConfig);
    }

    public <T> T executeRateLimited(String ip, Supplier<T> sup) {
        return getRateLimiter(ip).executeSupplier(sup);
    }

    private RateLimiter getRateLimiter(String ip) {
        final String key = String.format("ip:%s", ip);
        ipRateLimiters.computeIfAbsent(key, registry::rateLimiter);
        return ipRateLimiters.get(key);
    }
}
