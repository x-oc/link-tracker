package backend.academy.scrapper.dto.request;

import backend.academy.scrapper.model.Filter;
import backend.academy.scrapper.model.Tag;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.util.List;

public record AddLinkRequest(
    @NotNull URI link,
    List<Tag> tags,
    List<Filter> filters
) {
}
