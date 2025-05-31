package backend.academy.scrapper.api.stackoverflow;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

public record StackOverflowAnswersDTO(
        Owner owner, String body, @JsonProperty("creation_date") OffsetDateTime creationDate) {
    public record Owner(@JsonProperty("display_name") String name) {}
}
