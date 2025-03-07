package backend.academy.bot.service;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.dto.request.AddLinkRequest;
import backend.academy.bot.dto.request.RemoveLinkRequest;
import backend.academy.bot.model.Link;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RemoteLinksStorage implements LinksStorage {

    private final ScrapperClient scrapperClient;

    @Override
    public String registerUser(Long id) {
        var response = scrapperClient.registerChat(id);
        if (response.getStatusCode().isError()) {
            return response.getBody().toString();
        }
        return Responses.REGISTER_USER_SUCCESS.message;
    }

    @Override
    public String addUserLink (Long userId, String url, List<String> tags, List<String> filters) {
        var response = scrapperClient.addLink(userId, new AddLinkRequest(URI.create(url), tags, filters));
        if (response.isError()) {
            return response.apiErrorResponse().description();
        }
        return Responses.ADD_USER_LINK_SUCCESS.message;
    }

    @Override
    public String removeUserLink(Long userId, String url) {
        try {
            var response = scrapperClient.removeLink(userId, new RemoveLinkRequest(URI.create(url)));
            if (response.isError()) {
                return response.apiErrorResponse().description();
            }
            return Responses.REMOVE_USER_LINK_SUCCESS.message;
        } catch (IllegalArgumentException e) {
            return Responses.REMOVE_USER_LINK_FAIL.message;
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
