package backend.academy.scrapper;

import backend.academy.scrapper.api.InformationProvider;
import backend.academy.scrapper.api.LinkInformation;
import backend.academy.scrapper.config.ScrapperConfig;
import backend.academy.scrapper.dto.request.LinkUpdate;
import backend.academy.scrapper.model.Link;
import backend.academy.scrapper.sender.LinkUpdateSender;
import backend.academy.scrapper.service.LinkService;
import java.net.URI;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LinkUpdateScheduler {

    private final LinkService linkService;
    private final ScrapperConfig scrapperConfig;
    private final Map<String, InformationProvider> informationProviders;
    private final LinkUpdateSender sender;

    @Scheduled(fixedDelayString = "#{@'app-backend.academy.scrapper.config.ScrapperConfig'.scheduler.interval}")
    public void update() {
        log.info("Update started.");
        linkService
                .listOldLinks(
                        scrapperConfig.scheduler().forceCheckDelay(),
                        scrapperConfig.scheduler().maxLinksPerCheck())
                .forEach(link -> {
                    log.atInfo()
                            .setMessage("Updating link.")
                            .addKeyValue("link", link.url())
                            .log();
                    URI uri = URI.create(link.url());
                    InformationProvider provider = informationProviders.get(uri.getHost());
                    LinkInformation linkInformation = provider.fetchInformation(uri);
                    linkInformation = provider.filter(linkInformation, link.lastUpdated(), link.metaInformation());
                    processLinkInformation(linkInformation, link);
                });
        log.info("Update finished.");
    }

    private void processLinkInformation(LinkInformation linkInformation, Link link) {
        if (linkInformation.events().isEmpty()) {
            linkService.checkNow(link.url());
            return;
        }
        linkService.update(
            link.url(),
            linkInformation.events().getFirst().lastModified(),
            linkInformation.metaInformation());
        var subscribers = linkService.getLinkSubscribers(link.id()).stream().toList();
        linkInformation
                .events()
                .reversed()
                .forEach(event -> sender.sendUpdate(
                        new LinkUpdate(link.id(), URI.create(link.url()), event.description(), subscribers)));
    }
}
