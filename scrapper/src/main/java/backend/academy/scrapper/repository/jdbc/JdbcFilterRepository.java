package backend.academy.scrapper.repository.jdbc;

import backend.academy.scrapper.repository.FilterRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JdbcFilterRepository implements FilterRepository {

    private final JdbcClient client;

    @Override
    public List<String> findByLink(long linkId) {
        return client.sql("SELECT filter.filter FROM filter WHERE link_id = :link_id")
                .param("link_id", linkId)
                .query(String.class)
                .list();
    }

    @Override
    public void add(long linkId, String filter) {
        client.sql("INSERT INTO filter(filter, link_id) VALUES (:filter, :link_id)")
                .param("link_id", linkId)
                .param("filter", filter)
                .update();
    }

    @Override
    public void removeByLink(long linkId) {
        client.sql("DELETE FROM filter WHERE link_id = :link_id")
                .param("link_id", linkId)
                .update();
    }
}
