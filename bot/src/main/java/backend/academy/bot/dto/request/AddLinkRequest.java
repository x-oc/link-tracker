package backend.academy.bot.dto.request;

import backend.academy.bot.model.Filter;
import backend.academy.bot.model.Tag;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.util.List;

public record AddLinkRequest(
    @NotNull URI link,
    List<Tag> tags,
    List<Filter> filters
) {
}
