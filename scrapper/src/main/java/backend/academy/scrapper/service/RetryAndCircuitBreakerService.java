package backend.academy.scrapper.service;

import backend.academy.scrapper.dto.request.LinkUpdate;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

@Service
public class RetryAndCircuitBreakerService {
    @Retry(name = "botRetry")
    @CircuitBreaker(name = "botCircuitBreaker")
    public void sendUpdateWithRetry(@NotNull Consumer<LinkUpdate> sup, LinkUpdate linkUpdate) {
        sup.accept(linkUpdate);
    }
}
