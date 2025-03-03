package backend.academy.scrapper.repository;

import backend.academy.scrapper.model.Filter;
import backend.academy.scrapper.model.Link;
import backend.academy.scrapper.model.Tag;
import org.springframework.stereotype.Component;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
public class InMemoryLinkRepository implements LinkRepository {

    List<Link> links = new ArrayList<>();
    long nextId = 1;

    @Override
    public List<Link> findAll() {
        System.out.println(links.size());
        return links;
    }

    @Override
    public long add(Link link) {
        link.id(nextId++);
        links.add(link);
        return link.id();
    }

    @Override
    public void remove(String link) {
        links.removeIf(link1 -> link1.url().equals(link));
    }

    @Override
    public Optional<Link> findByUrl(String url) {
        for (Link link : links) {
            if (link.url().equals(url)) {
                return Optional.of(link);
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Link> findByTag(Tag tag) {
        List<Link> answerLinks = new ArrayList<>();
        for (Link link : links) {
            if (link.tags().contains(tag.name())) {
                answerLinks.add(link);
            }
        }
        return answerLinks;
    }

    @Override
    public List<Link> findByFilter(Filter filter) {
        List<Link> answerLinks = new ArrayList<>();
        for (Link link : links) {
            if (link.filters().contains(filter.name())) {
                answerLinks.add(link);
            }
        }
        return answerLinks;
    }

    @Override
    public List<Link> findLinksCheckedAfter(Duration after, int limit) {
        List<Link> answerLinks = new ArrayList<>();
        for (Link link : links) {
            if (link.lastChecked().isBefore(OffsetDateTime.now().minus(after))) {
                answerLinks.add(link);
            }
        }
        return answerLinks;
    }

    @Override
    public void checkNow(String url) {
        for (Link link : links) {
            if (link.url().equals(url)) {
                link.lastChecked(OffsetDateTime.now());
            }
        }
    }

    @Override
    public void update(String url, OffsetDateTime lastUpdated, String info) {
        for (Link link : links) {
            if (Objects.equals(link.url(), url)) {
                link.lastChecked(OffsetDateTime.now());
                link.lastUpdated(lastUpdated);
                link.metaInformation(info);
            }
        }
    }
}
