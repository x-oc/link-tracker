package backend.academy.scrapper.service.jdbc;

import backend.academy.scrapper.exception.ChatAlreadyRegisteredException;
import backend.academy.scrapper.exception.ChatNotFoundException;
import backend.academy.scrapper.repository.IntegrationEnvironment;
import backend.academy.scrapper.repository.jdbc.JdbcChatRepository;
import backend.academy.scrapper.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class JdbcChatServiceIntegrationTest extends IntegrationEnvironment {

    private final ChatService chatService;
    private final JdbcChatRepository chatRepository;

    @Test
    @Transactional
    @Rollback
    void registerChatShouldAddChatInDatabase() {
        chatService.registerChat(123L);
        Assertions.assertThat(chatRepository.isExists(123L)).isTrue();
    }

    @Test
    @Transactional
    @Rollback
    void registerChatShouldThrowExceptionWhenChatAlreadyExists() {
        chatService.registerChat(123L);
        Assertions.assertThatThrownBy(() -> chatService.registerChat(123L))
            .isInstanceOf(ChatAlreadyRegisteredException.class);
    }

    @Test
    @Transactional
    @Rollback
    void removeChatShouldRemoveChatFromDatabase() {
        chatService.registerChat(123L);
        chatService.deleteChat(123L);
        Assertions.assertThat(chatRepository.isExists(123L)).isFalse();
    }

    @Test
    @Transactional
    @Rollback
    void removeChatShouldThrowExceptionWhenChatNotExists() {
        Assertions.assertThatThrownBy(() -> chatService.deleteChat(123L))
            .isInstanceOf(ChatNotFoundException.class);
    }

    @DynamicPropertySource
    static void jdbcProperties(DynamicPropertyRegistry registry) {
        registry.add("app.database-access-type", () -> "jdbc");
    }
}
