package backend.academy.scrapper.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
public abstract class WebClientInformationProvider implements InformationProvider {

    protected WebClient webClient;

    public WebClientInformationProvider(WebClient webClient) {
        this.webClient = webClient;
    }

    public WebClientInformationProvider(String apiUrl) {
        this(WebClient.create(apiUrl));
    }

    protected <T> T executeRequest(String uri, Class<T> type, T defaultValue) {
        try {
            return webClient
                    .get()
                    .uri(uri)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), response -> {
                        log.atError()
                                .setMessage("Error while executing request.")
                                .addKeyValue("status", response.statusCode())
                                .addKeyValue("uri", uri)
                                .addKeyValue("response", response.bodyToMono(String.class))
                                .log();
                        return response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new RuntimeException(
                                        "HTTP error: %s, body: %s".formatted(response.statusCode(), body))));
                    })
                    .bodyToMono(type)
                    .block();
        } catch (Exception e) {
            log.atError()
                    .setMessage("Error while trying to execute StackOverflow API Request.")
                    .addKeyValue("message", e.getMessage())
                    .addKeyValue("uri", uri)
                    .log();
            return defaultValue;
        }
    }
}
