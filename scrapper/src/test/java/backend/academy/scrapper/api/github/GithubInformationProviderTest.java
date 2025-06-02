package backend.academy.scrapper.api.github;

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
public class GithubInformationProviderTest {

    private static GithubProvider provider;
    private static final ScrapperConfig EMPTY_CONFIG = new ScrapperConfig(null, null, null, null, null, null, null);

    @BeforeAll
    public static void setUp() {
        WireMockServer server = new WireMockServer(wireMockConfig().dynamicPort());
        server.stubFor(get(urlPathMatching("/repos/x-oc/test-repo/issues"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(readAll("/github-mock-answer.json"))));
        server.stubFor(
                get(urlPathMatching("/repos/no/nope")).willReturn(aResponse().withStatus(404)));
        server.start();

        provider = new GithubProvider(server.baseUrl(), EMPTY_CONFIG);
    }

    @SneakyThrows
    @Test
    public void getInformationShouldReturnCorrectInformation() {
        var info = provider.fetchInformation(new URI("https://github.com/x-oc/test-repo"));
        var correctInfo = List.of(
                "User x-oc opened new Issue or Pull Request 'PR that finally works!': That one PR",
                "User x-oc opened new Issue or Pull Request 'The one Issue': Issue that will finally work",
                "User x-oc opened new Issue or Pull Request 'New issue': Description for the issue",
                "User x-oc opened new Issue or Pull Request 'Title': Description");

        Assertions.assertThat(info)
                .extracting(LinkInformation::url, LinkInformation::title)
                .contains(new URI("https://github.com/x-oc/test-repo"), "test-repo");
        Assertions.assertThat(info.events()).map(LinkUpdateEvent::description).containsAll(correctInfo);
    }

    @SneakyThrows
    @Test
    public void getInformationShouldReturnNullWhenRepositoryNotFound() {
        var info = provider.fetchInformation(new URI("https://github.com/no/nope"));
        Assertions.assertThat(info).isNull();
    }

    @SneakyThrows
    @Test
    public void isSupportedShouldReturnTrueIfHostIsValid() {
        var info = provider.isSupported(new URI("https://github.com/no/nope"));
        Assertions.assertThat(info).isTrue();
    }

    @SneakyThrows
    @Test
    public void isSupportedShouldReturnFalseIfHostIsInValid() {
        var info = provider.isSupported(new URI("https://gitlab.com/no/nope"));
        Assertions.assertThat(info).isFalse();
    }
}
