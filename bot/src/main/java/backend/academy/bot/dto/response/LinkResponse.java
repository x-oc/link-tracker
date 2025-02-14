package backend.academy.bot.dto.response;

import backend.academy.bot.model.Filter;
import backend.academy.bot.model.Tag;
import java.net.URI;
import java.util.List;

public record LinkResponse(
    Long id,
    URI url,
    List<Tag> tags,
    List<Filter> filters
) {
}
