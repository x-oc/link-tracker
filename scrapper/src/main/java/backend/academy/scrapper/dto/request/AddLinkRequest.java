package backend.academy.scrapper.dto.request;

import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.util.List;

public record AddLinkRequest(@NotNull URI link, List<String> tags, List<String> filters) {}
