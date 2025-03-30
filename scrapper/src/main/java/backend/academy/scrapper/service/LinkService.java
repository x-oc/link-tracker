package backend.academy.scrapper.service;

import backend.academy.scrapper.dto.response.LinkResponse;
import backend.academy.scrapper.dto.response.ListLinksResponse;
import backend.academy.scrapper.model.Link;
import java.net.URI;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;

public interface LinkService {

    ListLinksResponse listLinks(Long tgChatId);

    LinkResponse addLink(URI link, Long tgChatId);

    LinkResponse removeLink(String url, Long tgChatId);

    List<Link> listOldLinks(Duration after, int limit);

    void update(String url, OffsetDateTime lastModified, String metaInformation);

    List<Long> getLinkSubscribers(long linkId);

    void checkNow(String url);
}
