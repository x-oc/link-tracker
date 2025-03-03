package backend.academy.scrapper.api;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

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
                .onStatus(
                    status -> status.is4xxClientError() || status.is5xxServerError(),
                    response -> {
                        System.err.println("Error response: " + response.statusCode());
                        return response.bodyToMono(String.class)
                            .flatMap(body -> Mono.error(new RuntimeException("HTTP error: " + response.statusCode() + ", body: " + body)));
                    }
                )
                .bodyToMono(type)
                .block();
        } catch (Exception e) {
            System.err.println("Error while trying to Execute SO API Request: " + e.getMessage());
            return defaultValue;
        }
    }
}
