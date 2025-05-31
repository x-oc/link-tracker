package backend.academy.scrapper.service.jpa;

import java.net.URI;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import backend.academy.scrapper.api.InformationProvider;
import backend.academy.scrapper.api.LinkInformation;
import backend.academy.scrapper.api.github.GithubProvider;
import backend.academy.scrapper.dto.response.LinkResponse;
import backend.academy.scrapper.model.Link;
import backend.academy.scrapper.repository.IntegrationEnvironment;
import backend.academy.scrapper.service.LinkService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.within;

@ActiveProfiles("test")
@SpringBootTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class JpaLinkServiceTest extends IntegrationEnvironment {

    private final LinkService linkService;
    private final JdbcClient client;
    private final EntityManager manager;
    @MockitoBean
    private Map<String, InformationProvider> providers;
    @MockitoBean
    private GithubProvider provider;

    @Test
    @Transactional
    @Rollback
    public void registerShouldCreateLinkInDatabase() {
        Mockito.when(providers.get(Mockito.any())).thenReturn(provider);
        Mockito.when(provider.fetchInformation(Mockito.any())).thenReturn(
            new LinkInformation(URI.create("https://example.com"), "Example", List.of())
        );
        Mockito.when(provider.isSupported(Mockito.any())).thenReturn(true);

        client.sql("INSERT INTO chat (id) VALUES (5)").update();

        linkService.addLink(URI.create("https://example.com"), 5L, List.of(), List.of());
        manager.flush();
        Assertions.assertThat(client.sql("SELECT COUNT(*) FROM link WHERE url = 'https://example.com'")
                .query(Long.class).single())
            .isEqualTo(1L);
        Assertions.assertThat(client.sql("SELECT COUNT(*) FROM chat_link WHERE chat_id = 5")
                .query(Long.class).single())
            .isEqualTo(1L);
    }

    @Test
    @Transactional
    @Rollback
    public void deleteShouldRemoveLinkFromDatabase() {
        client.sql("INSERT INTO chat (id) VALUES (5)").update();
        var linkId =
            client.sql("INSERT INTO link (url) VALUES ('https://example.com') RETURNING id")
                .query(Long.class)
                .single();
        client.sql("INSERT INTO chat_link (chat_id, link_id) VALUES (5, ?)").params(linkId).update();
        linkService.removeLink("https://example.com", 5L);
        manager.flush();
        Assertions.assertThat(client.sql("SELECT COUNT(*) FROM link WHERE url = 'https://example.com'")
                .query(Long.class).single())
            .isEqualTo(0L);
        Assertions.assertThat(client.sql("SELECT COUNT(*) FROM chat_link WHERE chat_id = 5")
                .query(Long.class).single())
            .isEqualTo(0L);
    }

    @Test
    @Transactional
    @Rollback
    public void updateShouldUpdateLinkInDatabase() {
        var linkId =
            client.sql("INSERT INTO link (url) VALUES ('https://example.com') RETURNING id")
                .query(Long.class)
                .single();
        OffsetDateTime dateTime = OffsetDateTime.now();
        linkService.update("https://example.com", dateTime);
        manager.flush();
        Assertions.assertThat(client.sql("SELECT last_updated FROM link WHERE id = ?").params(linkId)
                .query(OffsetDateTime.class)
                .single())
            .isCloseTo(dateTime, within(1, ChronoUnit.SECONDS));
    }

    @Test
    @Transactional
    @Rollback
    public void getLinkSubscribersShouldReturnSubscribers() {
        client.sql("INSERT INTO chat (id) VALUES (5)").update();
        var linkId =
            client.sql("INSERT INTO link (url) VALUES ('https://example.com') RETURNING id")
                .query(Long.class)
                .single();
        client.sql("INSERT INTO chat_link (chat_id, link_id) VALUES (5, ?)").params(linkId).update();
        Assertions.assertThat(linkService.getLinkSubscribers(linkId))
            .containsExactly(5L);
    }

    @Test
    @Rollback
    @Transactional
    public void listLinksShouldReturnLinks() {
        client.sql("INSERT INTO chat (id) VALUES (5)").update();
        var linkId = client.sql("INSERT INTO link (url) VALUES ('https://example.com') RETURNING id")
            .query(Long.class).single();
        client.sql("INSERT INTO chat_link (chat_id, link_id) VALUES (5, ?)").params(linkId).update();
        Assertions.assertThat(linkService.listLinks(5L).links())
            .contains(new LinkResponse(linkId, URI.create("https://example.com"), List.of(), List.of()));
    }

    @Test
    @Rollback
    @Transactional
    public void checkNowShouldCheckLinkToDatabase() {
        var linkId = client.sql("INSERT INTO link (url) VALUES ('https://example.com') RETURNING id")
            .query(Long.class).single();
        linkService.checkNow("https://example.com");
        manager.flush();
        Assertions.assertThat(client.sql("SELECT * FROM link WHERE id = ?").params(linkId)
                .query(Link.class).single().lastChecked().withOffsetSameInstant(ZoneOffset.ofHours(3)))
            .isEqualToIgnoringHours(OffsetDateTime.now(ZoneOffset.ofHours(3)));
    }

    @Test
    @Rollback
    @Transactional
    public void listOldLinksShouldReturnLinks() {
        client.sql(
                "INSERT INTO link (url, last_checked) VALUES ('https://example.com', ?)")
            .params(OffsetDateTime.now().minusDays(4))
            .update();
        client.sql(
                "INSERT INTO link (url, last_checked) VALUES ('https://example2.com',?)")
            .params(OffsetDateTime.now().minusDays(3))
            .update();
        client.sql(
                "INSERT INTO link (url, last_checked) VALUES ('https://example3.com', ?)")
            .params(OffsetDateTime.now().minusDays(2))
            .update();

        Assertions.assertThat(linkService.listOldLinks(Duration.ofDays(3).minusHours(1), 5))
            .map(Link::url)
            .containsExactlyInAnyOrder("https://example2.com", "https://example.com");
    }

    @DynamicPropertySource
    static void jpaProperties(DynamicPropertyRegistry registry) {
        registry.add("app.database-access-type", () -> "jpa");
    }
}
