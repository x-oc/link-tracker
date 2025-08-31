package backend.academy.scrapper.resilience;

import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

import backend.academy.scrapper.dto.request.LinkUpdate;
import backend.academy.scrapper.repository.IntegrationEnvironment;
import backend.academy.scrapper.sender.HttpLinkUpdateSender;
import backend.academy.scrapper.service.ResilienceLinkUpdateSender;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.web.client.HttpServerErrorException;

@ActiveProfiles("test")
@WireMockTest(httpPort = 8090)
@SpringBootTest(properties = "app.message-transport=HTTP")
@DirtiesContext
public class TransportFallbackTest extends IntegrationEnvironment {

    @MockitoSpyBean
    private ResilienceLinkUpdateSender updateSender;

    @Autowired
    private HttpLinkUpdateSender httpLinkUpdateSender;

    private final LinkUpdate linkUpdate = new LinkUpdate(1L, null, null, null);

    @Test
    void httpShouldFallbackToKafkaIfFailed() {
        updateSender.updateSender(httpLinkUpdateSender);
        doNothing().when(updateSender).fallback(any(LinkUpdate.class), any(HttpServerErrorException.class));

        stubFor(post(urlPathMatching("/updates")).willReturn(WireMock.serverError()));

        Assertions.assertDoesNotThrow(() -> updateSender.sendUpdateReliably(linkUpdate));

        Mockito.verify(updateSender, Mockito.times(1))
                .fallback(any(LinkUpdate.class), any(HttpServerErrorException.class));
    }
}
