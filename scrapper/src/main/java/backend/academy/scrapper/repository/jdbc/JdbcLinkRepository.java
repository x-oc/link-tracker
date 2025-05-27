package backend.academy.scrapper.repository.jdbc;

import backend.academy.scrapper.model.Link;
import backend.academy.scrapper.repository.LinkRepository;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JdbcLinkRepository implements LinkRepository {

    private final JdbcClient client;

    @Override
    public List<Link> findAll() {
        return client.sql("SELECT * FROM link").query(Link.class).list();
    }

    @Override
    public long add(Link link) {
        return client.sql(
                        """
                        INSERT INTO link(url, last_updated, last_checked)
                        VALUES (:link, :last_updated, :last_checked)
                        ON CONFLICT (url)
                        DO UPDATE SET
                        last_updated = :last_updated,
                        last_checked = :last_checked
                        RETURNING id""")
                .param("link", link.url())
                .param("last_updated", link.lastUpdated())
                .param("last_checked", link.lastChecked())
                .query(Long.class)
                .single();
    }

    @Override
    public void remove(String url) {
        client.sql("DELETE FROM link WHERE url = :url")
                .param("url", url)
                .query(Long.class)
                .single();
    }

    @Override
    public Optional<Link> findByUrl(String url) {
        return client.sql("SELECT * FROM link WHERE url = :url")
                .param("url", url)
                .query(Link.class)
                .optional();
    }

    @Override
    public List<Link> findLinksCheckedAfter(Duration after, int limit) {
        return client.sql(
                        """
                SELECT *
                FROM link
                WHERE last_checked < :last_checked
                ORDER BY last_checked
                LIMIT :limit
                """)
                .param("last_checked", OffsetDateTime.now().minus(after))
                .param("limit", limit)
                .query(Link.class)
                .list();
    }

    @Override
    public void update(String url, OffsetDateTime lastModified) {
        client.sql(
                        """
                UPDATE link
                SET last_checked = :last_checked,
                last_updated = :last_updated
                WHERE url = :url""")
                .param("last_checked", OffsetDateTime.now())
                .param("last_updated", lastModified)
                .param("url", url)
                .update();
    }

    @Override
    public void checkNow(String url) {
        client.sql("UPDATE link SET last_checked = :last_checked WHERE url = :url")
                .param("last_checked", OffsetDateTime.now())
                .param("url", url)
                .update();
    }
}
