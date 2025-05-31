package backend.academy.scrapper.api.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

public record GithubEventDTO(
        @JsonProperty("created_at") OffsetDateTime creationDate, String title, String body, String state, User user) {

    public record User(String login) {}
}
