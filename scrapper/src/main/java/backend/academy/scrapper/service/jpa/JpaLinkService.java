package backend.academy.scrapper.service.jpa;

import backend.academy.scrapper.api.InformationProvider;
import backend.academy.scrapper.dto.response.LinkResponse;
import backend.academy.scrapper.dto.response.ListLinksResponse;
import backend.academy.scrapper.exception.ChatNotFoundException;
import backend.academy.scrapper.exception.LinkNotSupportedException;
import backend.academy.scrapper.model.Link;
import backend.academy.scrapper.repository.jpa.JpaChatRepository;
import backend.academy.scrapper.repository.jpa.JpaFilterRepository;
import backend.academy.scrapper.repository.jpa.JpaLinkRepository;
import backend.academy.scrapper.repository.jpa.JpaTagRepository;
import backend.academy.scrapper.repository.jpa.entity.ChatEntity;
import backend.academy.scrapper.repository.jpa.entity.FilterEntity;
import backend.academy.scrapper.repository.jpa.entity.LinkEntity;
import backend.academy.scrapper.repository.jpa.entity.TagEntity;
import backend.academy.scrapper.service.LinkService;
import java.net.URI;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class JpaLinkService implements LinkService {

    private final JpaLinkRepository linkRepository;
    private final JpaChatRepository chatRepository;
    private final JpaTagRepository tagRepository;
    private final JpaFilterRepository filterRepository;
    private final Map<String, InformationProvider> informationProviders;

    @Override
    @Transactional
    public ListLinksResponse listLinks(Long tgChatId) {
        return chatRepository
                .findById(tgChatId)
                .map(chat -> new ListLinksResponse(
                        chat.links().stream()
                                .map(LinkEntity::toDto)
                                .map(link -> new LinkResponse(
                                        link.id(), URI.create(link.url()), link.tags(), link.filters()))
                                .toList(),
                        chat.links().size()))
                .orElseThrow(() -> new ChatNotFoundException(tgChatId));
    }

    @Override
    @Transactional
    public LinkResponse addLink(URI link, Long tgChatId, List<String> tags, List<String> filters) {
        var chat = chatRepository.findById(tgChatId).orElseThrow(() -> new ChatNotFoundException(tgChatId));
        var provider = informationProviders.get(link.getHost());
        if (provider == null || !provider.isSupported(link)) {
            throw new LinkNotSupportedException(link.toString());
        }
        var linkInformation = provider.fetchInformation(link);
        if (linkInformation == null) {
            throw new LinkNotSupportedException(link.toString());
        }
        var lastModified = OffsetDateTime.now();
        if (!linkInformation.events().isEmpty()) {
            lastModified = linkInformation.events().getFirst().lastModified();
        }
        var optionalLink = linkRepository.findByUrl(link.toString());
        if (optionalLink.isPresent()) {
            LinkEntity linkEntity = optionalLink.orElseThrow();
            linkEntity.lastUpdated(lastModified);
            linkEntity.lastChecked(OffsetDateTime.now());
            linkEntity.metaInformation(linkInformation.metaInformation());
            chat.addLink(linkEntity);
            return new LinkResponse(linkEntity.id(), link, new ArrayList<>(), new ArrayList<>());
        }
        var linkEntity =
                new LinkEntity(link.toString(), lastModified, OffsetDateTime.now(), linkInformation.metaInformation());
        linkRepository.save(linkEntity);
        chat.addLink(linkEntity);
        if (tags != null) {
            for (var tag : tags) {
                tagRepository.save(new TagEntity(linkEntity, tag));
            }
        }

        if (filters != null) {
            for (var filter : filters) {
                filterRepository.save(new FilterEntity(linkEntity, filter));
            }
        }
        return new LinkResponse(linkEntity.id(), link, new ArrayList<>(), new ArrayList<>());
    }

    @Override
    @Transactional
    public LinkResponse removeLink(String url, Long tgChatId) {
        var chat = chatRepository.findById(tgChatId).orElseThrow(() -> new ChatNotFoundException(tgChatId));
        var linkEntity = linkRepository.findByUrl(url).orElseThrow();
        chat.removeLink(linkEntity);
        tagRepository.deleteByLink(linkEntity);
        filterRepository.deleteByLink(linkEntity);
        if (linkEntity.chats().isEmpty()) {
            linkRepository.delete(linkEntity);
        }
        var link = linkEntity.toDto();
        return new LinkResponse(linkEntity.id(), URI.create(url), link.tags(), link.filters());
    }

    @Override
    @Transactional
    public List<Link> listOldLinks(Duration after, int limit) {
        return linkRepository.findAllByLastCheckedBefore(OffsetDateTime.now().minus(after), Limit.of(limit)).stream()
                .map(LinkEntity::toDto)
                .toList();
    }

    @Override
    @Transactional
    public void update(String url, OffsetDateTime lastModified, String metaInformation) {
        var link = linkRepository.findByUrl(url).orElseThrow();
        link.lastUpdated(lastModified);
        link.metaInformation(metaInformation);
    }

    @Override
    @Transactional
    public List<Long> getLinkSubscribers(long linkId) {
        var link = linkRepository.findById(linkId).orElseThrow();
        return link.chats().stream().map(ChatEntity::id).toList();
    }

    @Override
    @Transactional
    public void checkNow(String url) {
        var link = linkRepository.findByUrl(url).orElseThrow();
        link.lastChecked(OffsetDateTime.now());
    }
}
