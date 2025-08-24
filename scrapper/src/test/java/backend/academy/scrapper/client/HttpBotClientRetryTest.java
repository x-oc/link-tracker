package backend.academy.scrapper.client;

import backend.academy.scrapper.dto.request.LinkUpdate;
import backend.academy.scrapper.repository.IntegrationEnvironment;
import backend.academy.scrapper.sender.HttpLinkUpdateSender;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;


@ActiveProfiles("test")
@WireMockTest(httpPort = 8090)
@SpringBootTest(properties = "app.message-transport=HTTP")
@DirtiesContext
public class HttpBotClientRetryTest extends IntegrationEnvironment {

    @Autowired
    private HttpLinkUpdateSender updateSender;

    private final LinkUpdate linkUpdate = new LinkUpdate(1L, null, null, null);

    @Test
    public void shouldRetryOnSpecifiedErrors() {

        stubFor(post(
            urlPathMatching("/updates"))
            .inScenario("Retry Scenario")
            .whenScenarioStateIs(STARTED)
            .willReturn(WireMock.serverError())
            .willSetStateTo("Second Attempt")
        );

        stubFor(post(
            urlPathMatching("/updates"))
            .inScenario("Retry Scenario")
            .whenScenarioStateIs("Second Attempt")
            .willReturn(WireMock.serviceUnavailable())
            .willSetStateTo("Third Attempt")
        );

        stubFor(post(
            urlPathMatching("/updates"))
            .inScenario("Retry Scenario")
            .whenScenarioStateIs("Third Attempt")
            .willReturn(WireMock.ok()
                .withHeader("Content-Type", "application/json")
                .withBody("{}")
            )
        );

        assertDoesNotThrow(() -> updateSender.sendUpdate(linkUpdate));

        verify(3, postRequestedFor(WireMock.urlEqualTo("/updates")));
    }

    @Test
    public void shouldNotRetryOnClientErrors() {
        stubFor(post(urlPathMatching("/updates")).willReturn(WireMock.badRequest()));

        assertThrows(HttpClientErrorException.class, () -> updateSender.sendUpdate(linkUpdate));

        verify(1, postRequestedFor(urlPathMatching("/updates")));
    }
}
