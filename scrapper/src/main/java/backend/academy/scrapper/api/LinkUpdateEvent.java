package backend.academy.scrapper.api;

import java.time.OffsetDateTime;
import java.util.Map;
import lombok.Builder;

@Builder
public record LinkUpdateEvent(String description, OffsetDateTime lastModified, Map<String, String> additionalData) {}
