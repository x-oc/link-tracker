package backend.academy.scrapper.api.github;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.List;

@JsonDeserialize(using = GithubEventsHolderDeserializer.class)
public record GithubEventsHolder(List<GithubEventInfo> events) {
    public static final GithubEventsHolder EMPTY = new GithubEventsHolder(null);
}
