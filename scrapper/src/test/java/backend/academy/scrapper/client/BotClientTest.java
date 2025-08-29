package backend.academy.scrapper.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

import backend.academy.scrapper.dto.OptionalAnswer;
import backend.academy.scrapper.dto.request.LinkUpdate;
import backend.academy.scrapper.dto.response.ApiErrorResponse;
import backend.academy.scrapper.repository.IntegrationEnvironment;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.net.URI;
import java.util.List;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;

@ActiveProfiles("test")
@WireMockTest(httpPort = 8090)
@SpringBootTest
public class BotClientTest extends IntegrationEnvironment {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private BotClient botClient;

    @SneakyThrows
    @Test
    public void updatesShouldReturnCorrectValue() {
        stubFor(post(urlPathMatching("/updates")).willReturn(aResponse().withStatus(200)));

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

        HttpClientErrorException exception = assertThrows(
                HttpClientErrorException.class,
                () -> botClient.handleUpdates(
                        new LinkUpdate(100L, URI.create("https://article.com"), "description", List.of())));
        assertEquals(404, exception.getStatusCode().value());
    }
}
