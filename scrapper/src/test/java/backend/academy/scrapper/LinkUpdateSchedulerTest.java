package backend.academy.scrapper;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.api.InformationProvider;
import backend.academy.scrapper.api.LinkInformation;
import backend.academy.scrapper.api.LinkUpdateEvent;
import backend.academy.scrapper.config.ScrapperConfig;
import backend.academy.scrapper.dto.request.LinkUpdate;
import backend.academy.scrapper.model.Link;
import backend.academy.scrapper.sender.LinkUpdateSender;
import backend.academy.scrapper.service.LinkService;
import java.net.URI;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(classes = TestApplication.class)
class LinkUpdateSchedulerTest {

    @Mock
    private Map<String, InformationProvider> providers;

    @Mock
    private LinkUpdateSender sender;

    @Mock
    private LinkService linkService;

    private final String url = "https://github.com/x-oc/slae-solutions";
    private final Link link = new Link(url, null, null, null, null);

    private LinkUpdateScheduler linkUpdateScheduler;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        ScrapperConfig config = new ScrapperConfig(
                null,
                null,
                new ScrapperConfig.Scheduler(true, Duration.ofSeconds(100), Duration.ofMillis(100), 10),
                null,
                null);
        linkUpdateScheduler = new LinkUpdateScheduler(linkService, config, providers, sender);
    }

    @Test
    @DisplayName("Проверка, что обновления отправляются только подписчикам ссылки")
    public void updateShouldSendUpdatesOnlyToLinkSubscribers() {
        when(linkService.listOldLinks(Duration.ofMillis(100), 10)).thenReturn(List.of(link));
        when(linkService.getLinkSubscribers(0)).thenReturn(List.of(1L, 2L));

        InformationProvider provider = mock(InformationProvider.class);
        when(providers.get("github.com")).thenReturn(provider);

        LinkInformation linkInformation = new LinkInformation(
                URI.create(url),
                "Программа на python для решения СЛАУ",
                List.of(new LinkUpdateEvent("update", OffsetDateTime.now())));
        when(provider.fetchInformation(URI.create(url))).thenReturn(linkInformation);
        when(provider.filter(linkInformation, link.lastUpdated())).thenReturn(linkInformation);

        linkUpdateScheduler.update();

        verify(sender, times(1))
                .sendUpdate(new LinkUpdate(
                        0L, URI.create(url), linkInformation.events().getFirst().description(), List.of(1L, 2L)));
        verify(linkService, times(1))
                .update(url, linkInformation.events().getFirst().lastUpdated());
    }

    @Test
    @DisplayName("Проверка, что обновления не отправляются, если событий нет")
    public void updateShouldNotSendUpdatesIfNoEvents() {
        when(linkService.listOldLinks(Duration.ofMillis(100), 10)).thenReturn(List.of(link));

        InformationProvider provider = mock(InformationProvider.class);
        when(providers.get("github.com")).thenReturn(provider);

        LinkInformation linkInformation =
                new LinkInformation(URI.create(url), "Программа на python для решения СЛАУ", List.of());
        when(provider.fetchInformation(URI.create(url))).thenReturn(linkInformation);
        when(provider.filter(linkInformation, link.lastUpdated()))
                .thenReturn(linkInformation);

        linkUpdateScheduler.update();

        verify(sender, never()).sendUpdate(any());
        verify(linkService, times(1)).checkNow(url);
    }
}
