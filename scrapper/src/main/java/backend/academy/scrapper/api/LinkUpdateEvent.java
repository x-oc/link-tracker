package backend.academy.scrapper.api;

import java.time.OffsetDateTime;
import lombok.Builder;

@Builder
public record LinkUpdateEvent(String description, OffsetDateTime lastUpdated) {}
