package backend.academy.scrapper.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;

import backend.academy.scrapper.dto.OptionalAnswer;
import backend.academy.scrapper.dto.request.LinkUpdate;
import backend.academy.scrapper.dto.response.ApiErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.net.URI;
import java.util.List;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import reactor.core.publisher.Mono;

@WireMockTest(httpPort = 9090)
public class BotClientTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @SneakyThrows
    @Test
    public void updatesShouldReturnCorrectValue() {
        stubFor(post(urlPathMatching("/updates")).willReturn(aResponse().withStatus(200)));

        BotClient botClient = botClient();
        OptionalAnswer<Void> response = botClient.handleUpdates(
                new LinkUpdate(100L, URI.create("https://article.com"), "description", List.of()));
        Assertions.assertThat(response).isNull();
    }

    @SneakyThrows
    @Test
    public void updatesShouldReturnErrorWhenResponseIsNotOk() {
        stubFor(post(urlPathMatching("/updates"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withBody(objectMapper.writeValueAsString(
                                new ApiErrorResponse("Not found", "404", "Not found", "Not found", List.of())))
                        .withHeader("Content-Type", "application/json")));

        BotClient scrapperClient = botClient();
        ApiErrorResponse response = scrapperClient
                .handleUpdates(new LinkUpdate(100L, URI.create("https://article.com"), "description", List.of()))
                .apiErrorResponse();
        Assertions.assertThat(response).extracting(ApiErrorResponse::code).isEqualTo("404");
    }

    private static BotClient botClient() {
        WebClient webClient = WebClient.builder()
                .defaultStatusHandler(httpStatusCode -> true, clientResponse -> Mono.empty())
                .baseUrl("http://localhost:9090")
                .build();

        HttpServiceProxyFactory httpServiceProxyFactory = HttpServiceProxyFactory.builderFor(
                        WebClientAdapter.create(webClient))
                .build();
        return httpServiceProxyFactory.createClient(BotClient.class);
    }
}
