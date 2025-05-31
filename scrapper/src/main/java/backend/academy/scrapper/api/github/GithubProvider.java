package backend.academy.scrapper.api.github;

import backend.academy.scrapper.api.LinkInformation;
import backend.academy.scrapper.api.LinkUpdateEvent;
import backend.academy.scrapper.api.WebClientInformationProvider;
import backend.academy.scrapper.config.ScrapperConfig;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
public class GithubProvider extends WebClientInformationProvider {

    private static final Pattern REPOSITORY_PATTERN = Pattern.compile("https://github.com/(.+)/(.+)");
    private static final int MAX_PER_UPDATE = 10;

    @Autowired
    public GithubProvider(@Value("${provider.github.url}") String apiUrl, ScrapperConfig config) {
        super(WebClient.builder()
                .baseUrl(apiUrl)
                .defaultHeaders(headers -> {
                    if (config.githubToken() != null) {
                        headers.set("Authorization", "Bearer " + config.githubToken());
                    }
                })
                .build());
    }

    @Override
    public boolean isSupported(URI url) {
        return REPOSITORY_PATTERN.matcher(url.toString()).matches();
    }

    @Override
    public String getType() {
        return "github.com";
    }

    @Override
    public LinkInformation fetchInformation(URI url) {

        if (!isSupported(url)) {
            log.atWarn()
                    .setMessage("Trying to fetch unsupported url.")
                    .addKeyValue("url", url)
                    .addKeyValue("provider", "github")
                    .log();
            return null;
        }

        var uri = "/repos%s/issues?per_page=%s".formatted(url.getPath(), MAX_PER_UPDATE);
        var info = executeRequest(uri, GithubEventsHolder.class, GithubEventsHolder.EMPTY);

        if (info == null || info.equals(GithubEventsHolder.EMPTY)) {
            log.atWarn()
                    .setMessage("GitHub returned no info.")
                    .addKeyValue("uri", uri)
                    .log();
            return null;
        }

        String repoName = url.getPath().substring(1).split("/")[1];

        List<LinkUpdateEvent> events = info.events().stream()
                .map(item -> {
                    if (Objects.equals(item.state(), "open")) {
                        String body = item.body();
                        if (body == null) {
                            body = "No description";
                        }
                        body = body.length() > 200 ? "%s ...".formatted(body.substring(0, 200)) : body;
                        return new LinkUpdateEvent(
                                "User %s opened new Issue or Pull Request '%s': %s"
                                        .formatted(item.user().login(), item.title(), body),
                                item.creationDate());
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .toList();

        return new LinkInformation(url, !info.events().isEmpty() ? repoName : "", events);
    }

    @Override
    public LinkInformation filter(LinkInformation info, OffsetDateTime after) {
        List<LinkUpdateEvent> realUpdates = info.events().stream()
                .filter(event -> event.lastUpdated().isAfter(after))
                .toList();
        return new LinkInformation(info.url(), info.title(), realUpdates);
    }
}
