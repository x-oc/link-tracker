package backend.academy.scrapper.repository;

import backend.academy.scrapper.model.Link;
import backend.academy.scrapper.repository.inMemory.InMemoryChatLinkRepository;
import backend.academy.scrapper.repository.inMemory.InMemoryLinkRepository;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class InMemoryChatLinkRepositoryTest {

    private InMemoryChatLinkRepository chatLinkRepository;
    private InMemoryLinkRepository linkRepository;

    @BeforeEach
    public void setUp() {
        linkRepository = new InMemoryLinkRepository();
        chatLinkRepository = new InMemoryChatLinkRepository(linkRepository);
    }

    @Test
    @DisplayName("Проверка, что ссылка пользователя добавляется в tgChatLinkRepository")
    public void addShouldAddUserLinkToRepository() {
        Link link = new Link("url", null, null, null, null);
        chatLinkRepository.add(1L, "url");
        linkRepository.add(link);

        List<Link> userLinks = chatLinkRepository.findAllByChatId(1L);

        Assertions.assertThat(userLinks.size()).isEqualTo(1);
        Assertions.assertThat(userLinks).contains(link);
    }

    @Test
    @DisplayName("Проверка, что id из LinkRepository можно проверить на наличие")
    public void findAllByUrlShouldReturnAllSubscribedUsers() {
        Link foo = new Link("foo", null, null, null, null);
        Link bar = new Link("bar", null, null, null, null);
        chatLinkRepository.add(1L, foo.url());
        chatLinkRepository.add(2L, foo.url());
        chatLinkRepository.add(2L, bar.url());
        chatLinkRepository.add(3L, bar.url());
        linkRepository.add(foo);
        linkRepository.add(bar);

        List<Long> userIds = chatLinkRepository.findAllByUrl(foo.url());

        Assertions.assertThat(userIds.size()).isEqualTo(2);
        Assertions.assertThat(userIds.contains(1L)).isEqualTo(true);
        Assertions.assertThat(userIds.contains(2L)).isEqualTo(true);
    }
}
