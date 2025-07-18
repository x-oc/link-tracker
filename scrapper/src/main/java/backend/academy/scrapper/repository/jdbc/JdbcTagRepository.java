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
    public List<String> findByLinkAndChat(long linkId, long chatId) {
        return client.sql("""
                SELECT t.name
                FROM tag t
                JOIN link_tag lt ON t.id = lt.tag_id
                WHERE lt.link_id = :link_id AND t.chat_id = :chat_id""")
                .param("link_id", linkId)
                .param("chat_id", chatId)
                .query(String.class)
                .list();
    }

    @Override
    public List<Link> findByTag(String tag) {
        return client.sql(
                        """
                SELECT l.*
                FROM link l
                JOIN link_tag lt ON l.id = lt.link_id
                JOIN tag t ON lt.tag_id = t.id
                WHERE t.name = :tag""")
                .param("tag", tag)
                .query(Link.class)
                .list();
    }

    @Override
    public void add(long linkId, String tag, long chatId) {
        client.sql("""
                INSERT INTO tag(name, chat_id)
                VALUES (:tag, :chat_id)
                ON CONFLICT (name, chat_id) DO UPDATE
                SET name = EXCLUDED.name
                RETURNING id""")
            .param("tag", tag)
            .param("chat_id", chatId)
            .query(Long.class)
            .optional()
            .ifPresent(tagId ->
                client.sql("""
                    INSERT INTO link_tag(link_id, tag_id)
                    VALUES (:link_id, :tag_id)
                    ON CONFLICT DO NOTHING""")
                    .param("link_id", linkId)
                    .param("tag_id", tagId)
                    .update());
    }

    @Override
    public void removeByLinkId(long linkId) {
        client.sql("DELETE FROM link_tag WHERE link_id = :link_id")
                .param("link_id", linkId)
                .update();

        client.sql("""
                DELETE FROM tag t
                WHERE NOT EXISTS (
                    SELECT 1 FROM link_tag lt WHERE lt.tag_id = t.id
                )""")
            .update();
    }
}
