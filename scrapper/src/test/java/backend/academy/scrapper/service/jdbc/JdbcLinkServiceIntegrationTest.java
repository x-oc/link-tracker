package backend.academy.scrapper.service.jdbc;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import backend.academy.scrapper.api.InformationProvider;
import backend.academy.scrapper.api.LinkInformation;
import backend.academy.scrapper.api.github.GithubProvider;
import backend.academy.scrapper.exception.LinkNotFoundException;
import backend.academy.scrapper.model.Link;
import backend.academy.scrapper.repository.IntegrationEnvironment;
import backend.academy.scrapper.repository.jdbc.JdbcChatLinkRepository;
import backend.academy.scrapper.repository.jdbc.JdbcLinkRepository;
import backend.academy.scrapper.service.ChatService;
import backend.academy.scrapper.service.LinkService;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class JdbcLinkServiceIntegrationTest extends IntegrationEnvironment {
    private final LinkService linkService;
    private final ChatService chatService;
    private final JdbcLinkRepository linkRepository;
    private final JdbcChatLinkRepository chatLinkRepository;

    @MockitoBean
    private GithubProvider provider;
    @MockitoBean
    private Map<String, InformationProvider> providers;

    @Test
    @Transactional
    @Rollback
    void addLinkShouldAddLinkAndCreateIfNotExists() {
        String url = "https://github.com/x-oc/test-repo";
        Mockito.when(provider.fetchInformation(Mockito.any()))
            .thenReturn(new LinkInformation(URI.create(url), "github", List.of()));
        Mockito.when(provider.isSupported(Mockito.any())).thenReturn(true);
        Mockito.when(providers.get(Mockito.any())).thenReturn(provider);

        chatService.registerChat(123L);
        System.out.println(linkRepository.findAll());
        linkService.addLink(URI.create(url), 123L, List.of(), List.of());

        Assertions.assertThat(linkRepository.findByUrl(url).orElseThrow().url())
            .isEqualTo(url);
        Assertions.assertThat(chatLinkRepository.findAllByChatId(123L)).map(Link::url)
            .contains(url);
    }

    @Test
    @Transactional
    @Rollback
    void getLinkSubscriberShouldCorrectlyWork() {
        chatService.registerChat(123L);
        var id = linkRepository.add(
            new Link("url", List.of(), List.of(), OffsetDateTime.now(), OffsetDateTime.now()));
        chatLinkRepository.add(123L, id);

        var response = linkService.getLinkSubscribers(id);
        Assertions.assertThat(response).contains(123L);
    }

    @Test
    @Transactional
    @Rollback
    void removeLinkShouldThrowExceptionWhenLinkNotExist() {
        chatService.registerChat(123L);

        Assertions.assertThatThrownBy(() -> linkService.removeLink("1", 123L))
            .isInstanceOf(LinkNotFoundException.class);
    }

    @DynamicPropertySource
    static void jdbcProperties(DynamicPropertyRegistry registry) {
        registry.add("app.database-access-type", () -> "jdbc");
    }
}
