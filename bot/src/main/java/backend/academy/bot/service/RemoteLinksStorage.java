package backend.academy.bot.service;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.dto.request.AddLinkRequest;
import backend.academy.bot.dto.request.RemoveLinkRequest;
import backend.academy.bot.model.Filter;
import backend.academy.bot.model.Link;
import backend.academy.bot.model.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Primary
@Service
@RequiredArgsConstructor
public class RemoteLinksStorage implements LinksStorage {

    private final ScrapperClient scrapperClient;

    @Override
    public void registerUser(Long id) {
        scrapperClient.registerChat(id);
    }

    @Override
    public boolean addUserLink (Long userId, String url, List<Tag> tags, List<Filter> filters) {
        var response = scrapperClient.addLink(userId, new AddLinkRequest(URI.create(url), tags, filters));
        return !response.isError();
    }

    @Override
    public boolean removeUserLink(Long userId, String url) {
        try {
            var response = scrapperClient.removeLink(userId, new RemoveLinkRequest(URI.create(url)));
            return !response.isError();
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public List<Link> getLinks(Long userId) {
        var response = scrapperClient.listLinks(userId).answer();
        var linkDTOs = response.links();
        List<Link> links = new ArrayList<>();
        if (linkDTOs == null) {
            return links;
        }
        for (var link : linkDTOs) {
            links.add(new Link(link.url().toString()));
        }
        return links;
    }
}
