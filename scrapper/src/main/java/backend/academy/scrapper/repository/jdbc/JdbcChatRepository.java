package backend.academy.scrapper.repository.jdbc;

import backend.academy.scrapper.repository.TgChatRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JdbcChatRepository implements TgChatRepository {

    private final JdbcClient client;

    @Override
    public List<Long> findAll() {
        return client.sql("SELECT (id) FROM chat").query(Long.class).list();
    }

    @Override
    public void add(long chatId) {
        client.sql("INSERT INTO chat(id) VALUES (:chat_id)")
                .param("chat_id", chatId)
                .update();
    }

    @Override
    public void remove(long chatId) {
        client.sql("DELETE FROM chat WHERE id = :id").param("id", chatId).update();
    }

    @Override
    public boolean isExists(long chatId) {
        return client.sql("SELECT id FROM chat WHERE id = :id")
                .param("id", chatId)
                .query(Long.class)
                .optional()
                .isPresent();
    }
}
