package backend.academy.scrapper.repository.jdbc;

import backend.academy.scrapper.repository.IntegrationEnvironment;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class JdbcChatTest extends IntegrationEnvironment {

    private final JdbcChatRepository jdbcChatRepository;

    @Test
    @Transactional
    @Rollback
    void addShouldInsertChatInDatabase() {
        jdbcChatRepository.add(123L);
        Assertions.assertThat(jdbcChatRepository.findAll()).contains(123L);
    }

    @Test
    @Transactional
    @Rollback
    void removeShouldDeleteChatFromDatabase() {
        jdbcChatRepository.add(123L);
        jdbcChatRepository.remove(123L);
        Assertions.assertThat(jdbcChatRepository.findAll()).doesNotContain(123L);
    }
}
