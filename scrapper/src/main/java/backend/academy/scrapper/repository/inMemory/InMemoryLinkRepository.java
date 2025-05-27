package backend.academy.scrapper.repository.inMemory;

import backend.academy.scrapper.model.Link;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class InMemoryLinkRepository {

    List<Link> links = new ArrayList<>();
    long nextId = 1;

    public List<Link> findAll() {
        return links;
    }

    public long add(Link link) {
        Optional<Link> alreadyExisting = findByUrl(link.url());
        if (alreadyExisting.isEmpty()) {
            link.id(nextId++);
            links.add(link);
            return link.id();
        }
        return alreadyExisting.orElseThrow().id();
    }

    public void remove(String link) {
        links.removeIf(link1 -> link1.url().equals(link));
    }

    public Optional<Link> findByUrl(String url) {
        for (Link link : links) {
            if (link.url().equals(url)) {
                return Optional.of(link);
            }
        }
        return Optional.empty();
    }

    public List<Link> findByTag(String tag) {
        List<Link> answerLinks = new ArrayList<>();
        for (Link link : links) {
            if (link.tags().contains(tag)) {
                answerLinks.add(link);
            }
        }
        return answerLinks;
    }

    public List<Link> findLinksCheckedAfter(Duration after, int limit) {
        List<Link> answerLinks = new ArrayList<>();
        for (Link link : links) {
            if (link.lastChecked().isBefore(OffsetDateTime.now().minus(after))) {
                answerLinks.add(link);
            }
        }
        return answerLinks;
    }

    public void checkNow(String url) {
        for (Link link : links) {
            if (link.url().equals(url)) {
                link.lastChecked(OffsetDateTime.now());
            }
        }
    }

    public void update(String url, OffsetDateTime lastUpdated) {
        for (Link link : links) {
            if (Objects.equals(link.url(), url)) {
                link.lastChecked(OffsetDateTime.now());
                link.lastUpdated(lastUpdated);
            }
        }
    }
}
