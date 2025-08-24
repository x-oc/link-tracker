package backend.academy.scrapper.client;

import backend.academy.scrapper.dto.request.LinkUpdate;
import backend.academy.scrapper.repository.IntegrationEnvironment;
import backend.academy.scrapper.sender.HttpLinkUpdateSender;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpServerErrorException;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.junit.jupiter.api.Assertions.assertThrows;


@ActiveProfiles("test")
@WireMockTest(httpPort = 8090)
@SpringBootTest(properties = "app.message-transport=HTTP")
@DirtiesContext
public class HttpBotClientCircuitBreakerTest extends IntegrationEnvironment {

    @Autowired
    private HttpLinkUpdateSender updateSender;

    private final LinkUpdate linkUpdate = new LinkUpdate(1L, null, null, null);

    @Test
    public void shouldOpenCircuitBreakerAfterMultipleFailures() {

        for (int i = 0; i < 10; i++) {
            stubFor(post(urlPathMatching("/updates"))
                .willReturn(WireMock.serverError()));
        }

        for (int i = 0; i < 2; i++) {
            assertThrows(HttpServerErrorException.class, () -> updateSender.sendUpdate(linkUpdate));
        }
        assertThrows(CallNotPermittedException.class, () -> updateSender.sendUpdate(linkUpdate));

        verify(8, postRequestedFor(WireMock.urlEqualTo("/updates")));
    }
}
