package backend.academy.bot.config;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.dto.response.ApiErrorResponse;
import backend.academy.bot.exception.ApiErrorException;
import backend.academy.bot.resilience.RetryInterceptor;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

@Configuration
public class ClientConfig {

    @Value("${scrapper.url}")
    private String scrapperUrl;
    @Value("${scrapper.response-timeout}")
    private Duration responseTimeout;

    @Bean
    public ScrapperClient scrapperClient(WebClient.Builder webClientBuilder, RetryRegistry retryRegistry) {
        Retry retry = retryRegistry.retry("scrapperRetry");
        WebClient webClient = webClientBuilder
            .clientConnector(new ReactorClientHttpConnector(
                HttpClient.create()
                    .responseTimeout(responseTimeout)
            ))
            .filter(RetryInterceptor.create(retry))
            .defaultStatusHandler(
                HttpStatusCode::isError,
                resp -> resp.bodyToMono(ApiErrorResponse.class)
                    .flatMap(error -> Mono.error(new ApiErrorException(error))))
            .defaultHeader("Content-Type", "application/json")
            .baseUrl(scrapperUrl)
            .build();

        return HttpServiceProxyFactory
            .builderFor(WebClientAdapter.create(webClient))
            .build()
            .createClient(ScrapperClient.class);
    }
}
