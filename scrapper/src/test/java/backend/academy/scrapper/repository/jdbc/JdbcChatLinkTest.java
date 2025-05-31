package backend.academy.scrapper.repository.jdbc;

import backend.academy.scrapper.model.Link;
import backend.academy.scrapper.repository.IntegrationEnvironment;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

@ActiveProfiles("test")
@SpringBootTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Testcontainers
public class JdbcChatLinkTest extends IntegrationEnvironment {

    private final JdbcChatRepository jdbcChatRepository;
    private final JdbcLinkRepository jdbcLinkRepository;
    private final JdbcChatLinkRepository jdbcChatLinkRepository;
    private final DataSource dataSource;

    @Test
    void checkConnection() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            System.out.println("URL: " + conn.getMetaData().getURL());
            System.out.println("Username: " + conn.getMetaData().getUserName());
        }
    }

    @Test
    @Transactional
    @Rollback
    void findAllByChatIdShouldFindValueByChatId() {
        jdbcChatRepository.add(123L);
        var url = "google.com";
        Link link = new Link(url, List.of(), List.of(), OffsetDateTime.MIN, OffsetDateTime.MAX);
        var id = jdbcLinkRepository.add(link);
        jdbcChatLinkRepository.add(123L, id);

        Assertions.assertThat(jdbcChatLinkRepository.findAllByChatId(123L)).map(Link::url).contains(url);
    }

    @Test
    @Transactional
    @Rollback
    void findAllByLinkIdShouldFindValueByLinkId() {
        jdbcChatRepository.add(123L);
        var id = jdbcLinkRepository.add(
            new Link("google.com", List.of(), List.of(), OffsetDateTime.MIN, OffsetDateTime.MAX));
        jdbcChatLinkRepository.add(123L, id);

        Assertions.assertThat(jdbcChatLinkRepository.findAllByLinkId(id)).contains(123L);
    }

    @Test
    @Transactional
    @Rollback
    void removeShouldDeleteValueFromDatabase() {
        jdbcChatRepository.add(123L);
        var url = "google.com";
        var id = jdbcLinkRepository.add(
            new Link(url, List.of(), List.of(), OffsetDateTime.MIN, OffsetDateTime.MAX));
        jdbcChatLinkRepository.add(123L, id);
        jdbcChatLinkRepository.remove(123L, id);

        Assertions.assertThat(jdbcChatLinkRepository.findAllByChatId(123L)).map(Link::url).doesNotContain(url);
    }
}
