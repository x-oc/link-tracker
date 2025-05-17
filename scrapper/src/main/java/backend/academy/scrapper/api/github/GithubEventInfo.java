package backend.academy.scrapper.api.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.util.Map;

public record GithubEventInfo(
        @JsonProperty("created_at") OffsetDateTime lastModified,
        String type,
        RepositoryInfo repo,
        EventPayload payload,
        Map<String, String> additionalData,
        Actor actor) {
    public record RepositoryInfo(String name) {}

    public record EventPayload(
            String size,
            String action,
            IssueCommentEventPayload issue,
            @JsonProperty("pull_request") PullRequestEventPayload pullRequest) {}

    public record IssueCommentEventPayload(String title, String body) {}

    public record PullRequestEventPayload(String title, String body) {}

    public record Actor(String login) {}
}
