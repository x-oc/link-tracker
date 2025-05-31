package backend.academy.scrapper.repository.jdbc;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import backend.academy.scrapper.model.Link;
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
public class JdbcLinkTest extends IntegrationEnvironment {

    private final JdbcLinkRepository jdbcLinkRepository;

    @Test
    @Transactional
    @Rollback
    void addShouldInsertLinkInDatabase() {
        var link = new Link("google.com", List.of(), List.of(), OffsetDateTime.MIN, OffsetDateTime.MAX);
        jdbcLinkRepository.add(link);
        var dbLink = jdbcLinkRepository.findByUrl(link.url());

        Assertions.assertThat(dbLink.orElseThrow())
            .extracting(Link::url)
            .isEqualTo(link.url());
    }

    @Test
    @Transactional
    @Rollback
    void removeShouldDeleteLinkFromDatabase() {
        var link = new Link("google.com", List.of(), List.of(), OffsetDateTime.MIN, OffsetDateTime.MAX);
        jdbcLinkRepository.add(link);
        jdbcLinkRepository.remove(link.url());

        Assertions.assertThat(jdbcLinkRepository.findByUrl(link.url()).isEmpty()).isTrue();
    }

    @Test
    @Transactional
    @Rollback
    void getAllShouldReturnAllLinks() {
        var link = new Link("google.com", List.of(), List.of(), OffsetDateTime.MIN, OffsetDateTime.MAX);
        var link2 = new Link("yandex.ru", List.of(), List.of(), OffsetDateTime.MIN, OffsetDateTime.MAX);
        jdbcLinkRepository.add(link);
        jdbcLinkRepository.add(link2);
        var dbLinks = jdbcLinkRepository.findAll();

        Assertions.assertThat(dbLinks).map(Link::url).contains(link.url(), link2.url());
    }

    @Test
    @Transactional
    @Rollback
    void findLinksCheckedAfterShouldReturnOldLinks() {
        var link = new Link("google.com", List.of(), List.of(),
            OffsetDateTime.MIN, OffsetDateTime.now());
        var link2 = new Link("yandex.ru", List.of(), List.of(),
            OffsetDateTime.MIN, OffsetDateTime.now().minus(Duration.ofDays(1)));
        jdbcLinkRepository.add(link);
        jdbcLinkRepository.add(link2);
        var dbLinks = jdbcLinkRepository.findLinksCheckedAfter(Duration.ofMinutes(10), 100);

        Assertions.assertThat(dbLinks).map(Link::url).contains(link2.url());
    }
}
