package backend.academy.bot.resilience;

import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.Retry;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import reactor.core.publisher.Mono;

public class RetryInterceptor {

    public static ExchangeFilterFunction create(Retry retry) {
        return ExchangeFilterFunction
            .ofRequestProcessor(
                request ->
                    Mono.just(ClientRequest.from(request).build()))
            .andThen(
                (request, next) ->
                    next.exchange(request).transformDeferred(RetryOperator.of(retry)));
    }
}
