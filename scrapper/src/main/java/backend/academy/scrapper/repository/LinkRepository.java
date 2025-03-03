package backend.academy.scrapper.repository;

import backend.academy.scrapper.model.Filter;
import backend.academy.scrapper.model.Link;
import backend.academy.scrapper.model.Tag;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface LinkRepository {

    List<Link> findAll();

    long add(Link link);

    void remove(String link);

    Optional<Link> findByUrl(String url);

    List<Link> findByTag(Tag tag);

    List<Link> findByFilter(Filter filter);

    List<Link> findLinksCheckedAfter(Duration after, int limit);

    void checkNow(String url);

    void update(String url, OffsetDateTime lastModified, String info);
}
