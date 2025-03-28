package backend.academy.scrapper.model;

import java.time.OffsetDateTime;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class Link {

    private long id;
    private String url;
    private List<String> tags;
    private List<String> filters;
    private OffsetDateTime lastChecked;
    private OffsetDateTime lastUpdated;
    private String metaInformation;

    public Link(
            String url,
            List<String> tags,
            List<String> filters,
            OffsetDateTime lastChecked,
            OffsetDateTime lastUpdated) {
        this.url = url;
        this.tags = tags;
        this.filters = filters;
        this.lastChecked = lastChecked;
        this.lastUpdated = lastUpdated;
    }
}
