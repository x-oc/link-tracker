package backend.academy.scrapper.api.stackoverflow;

import static backend.academy.scrapper.Utils.readAll;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import backend.academy.scrapper.api.LinkInformation;
import backend.academy.scrapper.api.LinkUpdateEvent;
import backend.academy.scrapper.config.ScrapperConfig;
import com.github.tomakehurst.wiremock.WireMockServer;
import java.net.URI;
import java.util.List;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
public class StackOverflowProviderTest {

    private static StackOverflowProvider provider;
    private static final ScrapperConfig EMPTY_CONFIG = new ScrapperConfig(null, null, null, null, null);

    @BeforeAll
    public static void setUp() {
        WireMockServer server = new WireMockServer(wireMockConfig().dynamicPort());
        server.stubFor(get(urlPathMatching("/questions/3648564"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(readAll("/stackoverflow-mock-answer.json"))));
        server.stubFor(get(urlPathMatching("/questions/3648564/answers"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(readAll("/stackoverflow-answers-mock-answer.json"))));
        server.stubFor(
                get(urlPathMatching("/questions/101.*")).willReturn(aResponse().withStatus(404)));
        server.start();
        provider = new StackOverflowProvider(server.baseUrl(), EMPTY_CONFIG);
    }

    @SneakyThrows
    @Test
    public void getInformationShouldReturnCorrectInformation() {
        var uri = new URI(
                "https://stackoverflow.com/questions/3648564/python-subclass-access-to-class-variable-of-parent");
        var info = provider.fetchInformation(uri);
        var correctInfo = List.of(
                "There is new answer by user Alex Martelli on the question "
                        + "'python subclass access to class variable of parent': "
                        + "<p>Python's scoping rules for barenames are very simple and straightforward: "
                        + "local namespace first, then (if any) outer functions in which the current one is nested, "
                        + "then globals, finally built-ins.   ...",
                "There is new answer by user Nathan Davis on the question "
                        + "'python subclass access to class variable of parent': "
                        + "<p>In Python, the body of a class is executed in its own namespace before the class is created "
                        + "(after which, the members of that namespace become the members of the class).  "
                        + "So when the interpreter re ...");

        Assertions.assertThat(info)
                .extracting(LinkInformation::url, LinkInformation::title)
                .contains(uri, "python subclass access to class variable of parent");
        Assertions.assertThat(info.events()).map(LinkUpdateEvent::description).containsAll(correctInfo);
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
