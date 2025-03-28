package backend.academy.scrapper.service;

import backend.academy.scrapper.api.InformationProvider;
import backend.academy.scrapper.api.LinkInformation;
import backend.academy.scrapper.dto.response.LinkResponse;
import backend.academy.scrapper.dto.response.ListLinksResponse;
import backend.academy.scrapper.exception.LinkAlreadyAddedException;
import backend.academy.scrapper.exception.LinkNotFoundException;
import backend.academy.scrapper.exception.LinkNotSupportedException;
import backend.academy.scrapper.model.Link;
import backend.academy.scrapper.repository.LinkRepository;
import backend.academy.scrapper.repository.TgChatLinkRepository;
import java.net.URI;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LinkService {

    private final LinkRepository linkRepository;
    private final TgChatLinkRepository tgChatLinkRepository;
    private final Map<String, InformationProvider> informationProviders;

    public ListLinksResponse listLinks(Long tgChatId) {
        var links = tgChatLinkRepository.findAllByChatId(tgChatId);
        var linkResponses = links.stream()
                .map(link -> new LinkResponse(link.id(), URI.create(link.url()), link.tags(), link.filters()))
                .toList();
        return new ListLinksResponse(linkResponses, linkResponses.size());
    }

    @Transactional
    public LinkResponse addLink(URI link, Long chatId) {
        if (linkRepository.findByUrl(link.toString()).isPresent()) {
            throw new LinkAlreadyAddedException(link);
        }
        InformationProvider provider = informationProviders.get(link.getHost());
        if (provider == null || !provider.isSupported(link)) {
            throw new LinkNotSupportedException(link.toString());
        }
        LinkInformation linkInformation = provider.fetchInformation(link);
        if (linkInformation == null) {
            throw new LinkNotSupportedException(link.toString());
        }
        OffsetDateTime lastModified = OffsetDateTime.now();
        if (!linkInformation.events().isEmpty()) {
            lastModified = linkInformation.events().getFirst().lastModified();
        }
        var id =
                linkRepository.add(new Link(link.toString(), List.of(), List.of(), OffsetDateTime.now(), lastModified));
        tgChatLinkRepository.add(chatId, link.toString());
        log.atInfo()
                .setMessage("Added new link.")
                .addKeyValue("chatId", chatId)
                .addKeyValue("link", link.toString())
                .log();
        return new LinkResponse(id, link, List.of(), List.of());
    }

    @Transactional
    public LinkResponse removeLink(URI url, Long chatId) {
        Optional<Link> optionalLink = linkRepository.findByUrl(url.toString());
        if (optionalLink.isPresent()) {
            Link link = optionalLink.orElseThrow();
            tgChatLinkRepository.remove(chatId, url.toString());
            if (tgChatLinkRepository.findAllByUrl(url.toString()).isEmpty()) {
                linkRepository.remove(url.toString());
            }
            log.atInfo()
                    .setMessage("Removed link.")
                    .addKeyValue("chatId", chatId)
                    .addKeyValue("link", link.toString())
                    .log();
            return new LinkResponse(link.id(), URI.create(link.url()), link.tags(), link.filters());
        } else {
            throw new LinkNotFoundException(url.toString());
        }
    }

    @Transactional
    public List<Link> listOldLinks(Duration after, int limit) {
        return linkRepository.findLinksCheckedAfter(after, limit);
    }

    @Transactional
    public void update(String url, OffsetDateTime lastModified, String info) {
        if (linkRepository.findByUrl(url).isEmpty()) {
            throw new LinkNotFoundException(url);
        }
        linkRepository.update(url, lastModified, info);
    }

    @Transactional
    public List<Long> getLinkSubscribers(String url) {
        return tgChatLinkRepository.findAllByUrl(url);
    }

    @Transactional
    public void checkNow(String url) {
        linkRepository.checkNow(url);
    }
}
