package backend.academy.scrapper.api.stackoverflow;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

public record StackOverflowItem(
    String title,
    @JsonProperty("last_activity_date") OffsetDateTime lastModified,
    @JsonProperty("answer_count") int answersCount,
    int score
) {
}
