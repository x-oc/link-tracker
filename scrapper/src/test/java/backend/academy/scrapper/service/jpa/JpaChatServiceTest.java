package backend.academy.scrapper.service.jpa;

import backend.academy.scrapper.repository.IntegrationEnvironment;
import backend.academy.scrapper.service.ChatService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class JpaChatServiceTest extends IntegrationEnvironment {

    private final ChatService chatService;
    private final EntityManager manager;
    private final JdbcClient client;

    @Test
    @Transactional
    @Rollback
    public void registerShouldCreateChatInDatabase() {
        chatService.registerChat(10L);
        manager.flush();
        Assertions.assertThat(client.sql("SELECT COUNT(*) FROM chat WHERE id = 10")
                        .query(Long.class)
                        .single())
                .isEqualTo(1L);
    }

    @Test
    @Transactional
    @Rollback
    public void deleteShouldRemoveChatFromDatabaseAndRelatedLinks() {
        client.sql("INSERT INTO chat (id) VALUES (10)").update();
        var link1 = client.sql("INSERT INTO link (url) VALUES ('https://example.com') RETURNING id")
                .query(Long.class)
                .single();
        var link2 = client.sql("INSERT INTO link (url) VALUES ('https://example2.com') RETURNING id")
                .query(Long.class)
                .single();
        client.sql("INSERT INTO chat_link (chat_id, link_id) VALUES (10, ?)")
                .params(link1)
                .update();

        // When
        chatService.deleteChat(10L);
        manager.flush();

        // Then
        Assertions.assertThat(client.sql("SELECT COUNT(*) FROM chat WHERE id = 10")
                        .query(Long.class)
                        .single())
                .isEqualTo(0L);
        Assertions.assertThat(client.sql("SELECT COUNT(*) FROM link WHERE id = ?")
                        .params(link1)
                        .query(Long.class)
                        .single())
                .isEqualTo(0L);
        Assertions.assertThat(client.sql("SELECT COUNT(*) FROM link WHERE id = ?")
                        .params(link2)
                        .query(Long.class)
                        .single())
                .isEqualTo(1L);
    }

    @DynamicPropertySource
    static void jpaProperties(DynamicPropertyRegistry registry) {
        registry.add("app.database-access-type", () -> "jpa");
    }
}
