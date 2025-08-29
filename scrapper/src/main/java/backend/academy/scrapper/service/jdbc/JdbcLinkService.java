package backend.academy.scrapper.service.jdbc;

import backend.academy.scrapper.api.InformationProvider;
import backend.academy.scrapper.api.LinkInformation;
import backend.academy.scrapper.dto.response.LinkResponse;
import backend.academy.scrapper.dto.response.ListLinksResponse;
import backend.academy.scrapper.exception.LinkAlreadyAddedException;
import backend.academy.scrapper.exception.LinkNotFoundException;
import backend.academy.scrapper.exception.LinkNotSupportedException;
import backend.academy.scrapper.model.Link;
import backend.academy.scrapper.repository.FilterRepository;
import backend.academy.scrapper.repository.LinkRepository;
import backend.academy.scrapper.repository.TagRepository;
import backend.academy.scrapper.repository.TgChatLinkRepository;
import backend.academy.scrapper.service.LinkService;
import java.net.URI;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class JdbcLinkService implements LinkService {

    private final LinkRepository linkRepository;
    private final TgChatLinkRepository tgChatLinkRepository;
    private final TagRepository tagRepository;
    private final FilterRepository filterRepository;
    private final Map<String, InformationProvider> informationProviders;

    @Override
    public ListLinksResponse listLinks(Long tgChatId) {
        var links = tgChatLinkRepository.findAllByChatId(tgChatId);
        var linkResponses = links.stream()
                .map(link -> {
                    List<String> tags = tagRepository.findByLinkAndChat(link.id(), tgChatId);
                    return new LinkResponse(link.id(), URI.create(link.url()), tags, link.filters());
                })
                .toList();
        return new ListLinksResponse(linkResponses, linkResponses.size());
    }

    @Override
    @Transactional
    public LinkResponse addLink(URI link, Long tgChatId, List<String> tags, List<String> filters) {
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
            lastModified = linkInformation.events().getFirst().lastUpdated();
        }
        if (tags == null) {
            tags = new ArrayList<>();
        }
        if (filters == null) {
            filters = new ArrayList<>();
        }
        var id = linkRepository.add(new Link(link.toString(), tags, filters, lastModified, OffsetDateTime.now()));
        tgChatLinkRepository.add(tgChatId, id);
        for (var tag : tags) {
            tagRepository.add(id, tag, tgChatId);
        }
        for (var filter : filters) {
            filterRepository.add(id, filter);
        }
        return new LinkResponse(id, link, tags, filters);
    }

    @Override
    @Transactional
    public LinkResponse removeLink(String url, Long tgChatId) {
        Optional<Link> optionalLink = linkRepository.findByUrl(url);
        if (optionalLink.isPresent()) {
            Link link = optionalLink.orElseThrow();
            tgChatLinkRepository.remove(tgChatId, link.id());
            tagRepository.removeByLinkId(link.id());
            filterRepository.removeByLink(link.id());
            if (tgChatLinkRepository.findAllByLinkId(link.id()).isEmpty()) {
                linkRepository.remove(url);
            }
            return new LinkResponse(link.id(), URI.create(link.url()), link.tags(), link.filters());
        } else {
            throw new LinkNotFoundException(url);
        }
    }

    @Override
    @Transactional
    public List<Link> listOldLinks(Duration after, int limit) {
        return linkRepository.findLinksCheckedAfter(after, limit);
    }

    @Override
    @Transactional
    public void update(String url, OffsetDateTime lastModified) {
        if (linkRepository.findByUrl(url).isEmpty()) {
            throw new LinkNotFoundException(url);
        }
        linkRepository.update(url, lastModified);
    }

    @Override
    @Transactional
    public List<Long> getLinkSubscribers(long linkId) {
        return tgChatLinkRepository.findAllByLinkId(linkId);
    }

    @Override
    @Transactional
    public void checkNow(String url) {
        linkRepository.checkNow(url);
    }
}
