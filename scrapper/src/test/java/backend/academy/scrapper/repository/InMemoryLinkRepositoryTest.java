package backend.academy.scrapper.repository;

import backend.academy.scrapper.model.Link;
import backend.academy.scrapper.repository.inMemory.InMemoryLinkRepository;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class InMemoryLinkRepositoryTest {

    private InMemoryLinkRepository linkRepository;

    @BeforeEach
    public void setUp() {
        linkRepository = new InMemoryLinkRepository();
    }

    @Test
    @DisplayName("Проверка, что вся информация о ссылке добавляется в LinkRepository")
    public void addShouldAddLinkToRepository() {
        Link link = new Link("url", List.of("tag"), List.of("filter"), null, null);

        linkRepository.add(link);
        List<Link> links = linkRepository.findAll();

        Assertions.assertThat(links.size()).isEqualTo(1);
        Assertions.assertThat(links).contains(link);
    }

    @Test
    @DisplayName("Проверка, что ссылку из LinkRepository можно получить по url")
    public void findByUrlShouldReturnCorrectLink() {
        Link link = new Link("url", List.of("tag"), List.of("filter"), null, null);

        linkRepository.add(link);

        Assertions.assertThat(linkRepository.findByUrl("url").orElse(new Link()))
                .isEqualTo(link);
    }

    @Test
    @DisplayName("Проверка на добавление дубля ссылки в LinkRepository")
    public void addSecondTimeShouldNotAddLink() {
        Link link = new Link("url", List.of("tag"), List.of("filter"), null, null);

        linkRepository.add(link);
        linkRepository.add(link);
        List<Link> links = linkRepository.findAll();

        Assertions.assertThat(links.size()).isEqualTo(1);
    }
}
