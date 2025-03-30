package backend.academy.scrapper.repository;

import backend.academy.scrapper.repository.inMemory.InMemoryChatRepository;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class InMemoryChatRepositoryTest {

    private TgChatRepository chatRepository;

    @BeforeEach
    public void setUp() {
        chatRepository = new InMemoryChatRepository();
    }

    @Test
    @DisplayName("Проверка, что id пользователя добавляется в tgChatRepository")
    public void addShouldAddUserIdToRepository() {
        chatRepository.add(1L);
        List<Long> userIds = chatRepository.findAll();

        Assertions.assertThat(userIds.size()).isEqualTo(1);
        Assertions.assertThat(userIds).contains(1L);
    }

    @Test
    @DisplayName("Проверка, что id из tgChatRepository можно проверить на наличие")
    public void isExistsShouldReturnTrueForExistingId() {
        chatRepository.add(1L);

        Assertions.assertThat(chatRepository.isExists(1L)).isEqualTo(true);
    }
}
