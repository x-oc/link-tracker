package backend.academy.bot.config;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = false)
public record ApplicationConfig(
    @NotEmpty String telegramToken,
    Kafka kafka,
    Redis redis,
    RateLimiterProperties rateLimiter) {
    public record Kafka(String updatesTopicName) {}

    public record Redis(int cacheTtl, String listCommandCachePrefix) {}

    public record RateLimiterProperties(Duration timeoutDuration, int limitForPeriod, Duration limitRefreshPeriod) {}
}
