package backend.academy.scrapper.repository.jdbc;

import backend.academy.scrapper.model.Link;
import backend.academy.scrapper.repository.TagRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JdbcTagRepository implements TagRepository {

    private final JdbcClient client;

    @Override
    public List<String> findByLink(long linkId) {
        return client.sql("SELECT tag.tag FROM tag WHERE link_id = :link_id")
                .param("link_id", linkId)
                .query(String.class)
                .list();
    }

    @Override
    public List<Link> findByTag(String tag) {
        return client.sql(
                        """
                SELECT l.*
                FROM link l
                JOIN tag t
                ON l.id = t.link_id
                WHERE t.tag = :tag""")
                .param("tag", tag)
                .query(Link.class)
                .list();
    }

    @Override
    public void add(long linkId, String tag) {
        client.sql("INSERT INTO tag(tag, link_id) VALUES (:tag, :link_id)")
                .param("link_id", linkId)
                .param("tag", tag)
                .update();
    }

    @Override
    public void removeByLinkId(long linkId) {
        client.sql("DELETE FROM tag WHERE link_id = :link_id")
                .param("link_id", linkId)
                .update();
    }
}
