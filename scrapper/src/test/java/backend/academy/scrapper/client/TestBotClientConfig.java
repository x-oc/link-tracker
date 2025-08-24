package backend.academy.scrapper.client;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

@Configuration
public class TestBotClientConfig {
//
//    private final String botUrl = "http://localhost:8090";
//
//    @Value("${bot.response-timeout}")
//    private Duration responseTimeout;
//
//    @Bean
//    public BotClient botClient(WebClient.Builder webClientBuilder,
//                               RetryRegistry retryRegistry,
//                               CircuitBreakerRegistry circuitBreakerRegistry) {
//        Retry retry = retryRegistry.retry("botRetry");
//        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("botCircuitBreaker");
//
//        WebClient webClient = webClientBuilder
//            .clientConnector(new ReactorClientHttpConnector(
//                HttpClient.create()
//                    .responseTimeout(responseTimeout)
//            ))
//            .filter(ResilienceInterceptor.create(retry, circuitBreaker))
//            .defaultStatusHandler(
//                HttpStatusCode::is5xxServerError,
//                clientResponse -> Mono.error(new HttpServerErrorException(clientResponse.statusCode())))
//            .defaultStatusHandler(
//                HttpStatusCode::is4xxClientError,
//                clientResponse -> Mono.error(new HttpClientErrorException(clientResponse.statusCode()))
//            )
//            .defaultHeader("Content-Type", "application/json")
//            .baseUrl(botUrl)
//            .build();
//
//        HttpServiceProxyFactory httpServiceProxyFactory = HttpServiceProxyFactory.builderFor(
//                WebClientAdapter.create(webClient))
//            .build();
//        return httpServiceProxyFactory.createClient(BotClient.class);
//    }
//
//    @Bean
//    public WebClient.Builder webClientBuilder() {
//        return WebClient.builder();
//    }
//
//    @Bean
//    public RetryRegistry retryRegistry() {
//        RetryConfig retryConfig = RetryConfig.custom()
//            .maxAttempts(3)
//            .waitDuration(Duration.ofMillis(400))
//            .retryOnException(throwable ->
//                throwable instanceof WebClientRequestException ||
//                throwable instanceof WebClientResponseException ||
//                throwable instanceof HttpServerErrorException ||
//                throwable instanceof IOException ||
//                throwable instanceof TimeoutException)
//            .build();
//
//        return RetryRegistry.of(retryConfig);
//    }
//
//    @Bean
//    public CircuitBreakerRegistry circuitBreakerRegistry() {
//        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
//            .failureRateThreshold(50)
//            .minimumNumberOfCalls(5)
//            .slidingWindowSize(10)
//            .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
//            .waitDurationInOpenState(Duration.ofSeconds(10))
//            .permittedNumberOfCallsInHalfOpenState(3)
//            .automaticTransitionFromOpenToHalfOpenEnabled(false)
//            .recordExceptions(
//                HttpServerErrorException.class,
//                IOException.class,
//                TimeoutException.class,
//                WebClientRequestException.class)
//            .ignoreExceptions(HttpClientErrorException.class)
//            .build();
//
//        return CircuitBreakerRegistry.of(circuitBreakerConfig);
//    }
}
