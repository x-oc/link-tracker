package backend.academy.scrapper.api.github;

import backend.academy.scrapper.api.EventCollectableInformationProvider;
import backend.academy.scrapper.api.LinkInformation;
import backend.academy.scrapper.api.LinkUpdateEvent;
import backend.academy.scrapper.config.ScrapperConfig;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
public class GithubProvider extends EventCollectableInformationProvider<GithubEventInfo> {

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
        registerCollector(
                "PushEvent",
                item -> new LinkUpdateEvent(
                        "%s new commits by user %s in '%s' repo!".formatted(
                            item.payload().size(),
                            item.repo().name(),
                            item.actor().login()),
                        item.lastModified(),
                        Map.of(
                                "size", item.payload().size(),
                                "repo", item.repo().name(),
                                "user", item.actor().login())));
        registerCollector(
                "IssueCommentEvent",
                item -> new LinkUpdateEvent(
                        "New message by user %s in '%s' issue!".formatted(
                            item.actor().login(),
                            item.payload().issue().title()),
                        item.lastModified(),
                        Map.of(
                                "title", item.payload().issue().title(),
                                "user", item.actor().login())));
        registerCollector(
                "IssuesEvent",
                item -> {
                    if (Objects.equals(item.payload().action(), "opened")) {
                        String body = item.payload().issue().body();
                        body = body.length() > 200 ? "%s ...".formatted(body.substring(0, 200)) : body;
                        return new LinkUpdateEvent(
                            "User %s opened new issue '%s' in '%s' repo: %s".formatted(
                                item.actor().login(),
                                item.payload().issue().title(),
                                item.repo().name(),
                                body
                            ),
                            item.lastModified(),
                            Map.of(
                                "title", item.payload().issue().title(),
                                "repo", item.repo().name(),
                                "user", item.actor().login(),
                                "body", body));
                    }
                    return new LinkUpdateEvent(
                        "Issue '%s' in '%s' repo is updated!".formatted(
                            item.payload().issue().title(),
                            item.repo().name()
                        ),
                        item.lastModified(),
                        Map.of(
                                "title", item.payload().issue().title(),
                                "repo", item.repo().name()));
                });
        registerCollector(
                "PullRequestEvent",
                item -> {
                    if (Objects.equals(item.payload().action(), "opened")) {
                        String body = item.payload().pullRequest().body();
                        body = body.length() > 200 ? "%s ...".formatted(body.substring(0, 200)) : body;
                        return new LinkUpdateEvent(
                            "User %s opened new pull request '%s' in '%s' repo: %s".formatted(
                                item.actor().login(),
                                item.payload().pullRequest().title(),
                                item.repo().name(),
                                body
                            ),
                            item.lastModified(),
                            Map.of(
                                "title", item.payload().pullRequest().title(),
                                "repo", item.repo().name(),
                                "user", item.actor().login(),
                                "body", body));
                    }
                    return new LinkUpdateEvent(
                        "Pull request '%s' in '%s' repo is updated!".formatted(
                            item.payload().pullRequest().title(),
                            item.repo().name()
                        ),
                        item.lastModified(),
                        Map.of(
                            "title", item.payload().pullRequest().title(),
                            "repo", item.repo().name()));
                });
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
        var uri = "/repos%s/events?per_page=%s".formatted(url.getPath(), MAX_PER_UPDATE);
        var info = executeRequest(uri, GithubEventsHolder.class, GithubEventsHolder.EMPTY);

        if (info == null || info.equals(GithubEventsHolder.EMPTY)) {
            log.atWarn()
                    .setMessage("GitHub returned no info.")
                    .addKeyValue("uri", uri)
                    .log();
            return null;
        }

        return new LinkInformation(
                url,
                !info.events().isEmpty() ? info.events().getFirst().repo().name() : "",
                info.events().stream()
                        .map(it -> {
                            var collector = getCollector(it.type());
                            if (collector == null) {
                                return new LinkUpdateEvent(
                                        "New update on github: %s".formatted(it.type().toLowerCase()),
                                        it.lastModified(),
                                        Map.of("type", it.type()));
                            }
                            return getCollector(it.type()).apply(it);
                        })
                        .toList());
    }

    @Override
    public LinkInformation filter(LinkInformation info, OffsetDateTime after, String optionalContext) {
        List<LinkUpdateEvent> realUpdates = info.events().stream()
                .filter(event -> event.lastModified().isAfter(after))
                .toList();
        return new LinkInformation(info.url(), info.title(), realUpdates);
    }
}
