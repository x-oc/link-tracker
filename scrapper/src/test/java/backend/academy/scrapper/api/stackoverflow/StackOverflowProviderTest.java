package backend.academy.scrapper.api.stackoverflow;

import static backend.academy.scrapper.Utils.readAll;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import backend.academy.scrapper.api.LinkInformation;
import backend.academy.scrapper.config.ScrapperConfig;
import com.github.tomakehurst.wiremock.WireMockServer;
import java.net.URI;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class StackOverflowProviderTest {

    private static StackOverflowProvider provider;
    private static final ScrapperConfig EMPTY_CONFIG = new ScrapperConfig(null, null, null, null);

    @BeforeAll
    public static void setUp() {
        WireMockServer server = new WireMockServer(wireMockConfig().dynamicPort());
        server.stubFor(get(urlPathMatching("/questions/100.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(readAll("/stackoverflow-mock-answer.json"))));
        server.stubFor(
                get(urlPathMatching("/questions/101.*")).willReturn(aResponse().withStatus(404)));
        server.start();
        provider = new StackOverflowProvider(server.baseUrl(), EMPTY_CONFIG);
    }

    @SneakyThrows
    @Test
    public void getInformationShouldReturnCorrectInformation() {
        var info = provider.fetchInformation(new URI("https://stackoverflow.com/questions/100/?hello_world"));
        Assertions.assertThat(info)
                .extracting(LinkInformation::url, LinkInformation::title)
                .contains(
                        new URI("https://stackoverflow.com/questions/100/?hello_world"),
                        "What is the &#39;--&gt;&#39; operator in C/C++?");
    }

    @SneakyThrows
    @Test
    public void getInformationShouldReturnNullWhenQuestionNotFound() {
        var info = provider.fetchInformation(new URI("https://stackoverflow.com/questions/101/?hello_world"));
        Assertions.assertThat(info).isNull();
    }

    @SneakyThrows
    @Test
    public void isSupportedShouldReturnTrueIfHostIsValid() {
        var info = provider.isSupported(new URI("https://stackoverflow.com/questions/100/?hello_world"));
        Assertions.assertThat(info).isTrue();
    }

    @SneakyThrows
    @Test
    public void isSupportedShouldReturnFalseIfHostIsInValid() {
        var info = provider.isSupported(new URI("https://memoryoutofrange.com/no/nope"));
        Assertions.assertThat(info).isFalse();
    }
}
