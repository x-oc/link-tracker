package backend.academy.scrapper.config;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = false)
public record ScrapperConfig(
        @NotEmpty String githubToken,
        StackOverflowCredentials stackOverflow,
        Scheduler scheduler,
        String databaseAccessType,
        KafkaProperties kafka,
        String migrationsPath,
        String messageTransport) {

    public record StackOverflowCredentials(@NotEmpty String key, @NotEmpty String accessToken) {}

    public record Scheduler(
            boolean enable, @NotNull Duration interval, @NotNull Duration forceCheckDelay, int maxLinksPerCheck) {}

    public record KafkaProperties(String updatesTopicName) {}
}
