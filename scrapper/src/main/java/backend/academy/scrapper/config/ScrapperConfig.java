package backend.academy.scrapper.config;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = false)
public record ScrapperConfig(
    @NotEmpty String githubToken,
    StackOverflowCredentials stackOverflow,
    Scheduler scheduler
) {

    public record StackOverflowCredentials(
        @NotEmpty String key,
        @NotEmpty String accessToken
    ) {
    }

    public record Scheduler(
        boolean enable,
        @NotNull Duration interval,
        @NotNull Duration forceCheckDelay,
        int maxLinksPerCheck
    ) {
    }
}
