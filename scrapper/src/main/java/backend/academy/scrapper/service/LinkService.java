package backend.academy.scrapper.service;

import java.net.URI;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import backend.academy.scrapper.api.InformationProvider;
import backend.academy.scrapper.api.LinkInformation;
import backend.academy.scrapper.dto.response.LinkResponse;
import backend.academy.scrapper.dto.response.ListLinksResponse;
import backend.academy.scrapper.model.Link;
import backend.academy.scrapper.repository.LinkRepository;
import backend.academy.scrapper.repository.TgChatLinkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public LinkResponse addLink(URI link, Long tgChatId) {
        if (linkRepository.findByUrl(link.toString()).isPresent()) {
            throw new RuntimeException(String.format("Link %s is already added", link));
        }
        InformationProvider provider = informationProviders.get(link.getHost());
        if (provider == null || !provider.isSupported(link)) {
            throw new RuntimeException(String.format("Link %s is not supported", link));
        }
        LinkInformation linkInformation = provider.fetchInformation(link);
        if (linkInformation == null) {
            throw new RuntimeException(String.format("Link %s is not supported", link));
        }
        OffsetDateTime lastModified = OffsetDateTime.now();
        if (!linkInformation.events().isEmpty()) {
            lastModified = linkInformation.events().getFirst().lastModified();
        }
        var id = linkRepository.add(new Link(
            link.toString(),
            List.of(),
            List.of(),
            OffsetDateTime.now(),
            lastModified
        ));
        tgChatLinkRepository.add(tgChatId, link.toString());
        return new LinkResponse(id, link, List.of(), List.of());
    }

    @Transactional
    public LinkResponse removeLink(URI url, Long tgChatId) {
        Optional<Link> optionalLink = linkRepository.findByUrl(url.toString());
        if (optionalLink.isPresent()) {
            Link link = optionalLink.get();
            tgChatLinkRepository.remove(tgChatId, url.toString());
            if (tgChatLinkRepository.findAllByUrl(url.toString()).isEmpty()) {
                linkRepository.remove(url.toString());
            }
            return new LinkResponse(link.id(), URI.create(link.url()), link.tags(), link.filters());
        } else {
            throw new RuntimeException(String.format("Link %s not found", url));
        }
    }

    @Transactional
    public List<Link> listOldLinks(Duration after, int limit) {
        return linkRepository.findLinksCheckedAfter(after, limit);
    }

    @Transactional
    public void update(String url, OffsetDateTime lastModified, String info) {
        if (linkRepository.findByUrl(url).isEmpty()) {
            throw new RuntimeException(String.format("Link %s not found", url));
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
