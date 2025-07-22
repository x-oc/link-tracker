package backend.academy.scrapper.config;

import backend.academy.scrapper.client.BotClient;
import backend.academy.scrapper.resilience.RetryInterceptor;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import java.time.Duration;

@Configuration
@EnableScheduling
@OpenAPIDefinition(info = @Info(title = "Scrapper API", description = "Scrapper API", version = "1.0.0"))
public class ClientConfig {

    @Value("${bot.url}")
    private String botUrl;
    @Value("${bot.response-timeout}")
    private Duration responseTimeout;

    @Bean
    public BotClient botClient(WebClient.Builder webClientBuilder, RetryRegistry retryRegistry) {
        Retry retry = retryRegistry.retry("botRetry");
        WebClient webClient = webClientBuilder
            .clientConnector(new ReactorClientHttpConnector(
                HttpClient.create()
                    .responseTimeout(responseTimeout)
            ))
            .filter(RetryInterceptor.create(retry))
            .defaultStatusHandler(httpStatusCode -> true, clientResponse -> Mono.empty())
            .defaultHeader("Content-Type", "application/json")
            .baseUrl(botUrl)
            .build();

        HttpServiceProxyFactory httpServiceProxyFactory = HttpServiceProxyFactory.builderFor(
                        WebClientAdapter.create(webClient))
                .build();
        return httpServiceProxyFactory.createClient(BotClient.class);
    }
}
