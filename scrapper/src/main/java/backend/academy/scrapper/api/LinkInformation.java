package backend.academy.scrapper.api;

import java.net.URI;
import java.util.List;

public record LinkInformation(
    URI url,
    String title,
    List<LinkUpdateEvent> events,
    String metaInformation
) {

    public LinkInformation(URI url, String title, List<LinkUpdateEvent> events) {
        this(url, title, events, "");
    }
}
