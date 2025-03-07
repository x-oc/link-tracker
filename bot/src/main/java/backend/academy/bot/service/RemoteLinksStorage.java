package backend.academy.bot.service;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.dto.request.AddLinkRequest;
import backend.academy.bot.dto.request.RemoveLinkRequest;
import backend.academy.bot.model.Link;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RemoteLinksStorage implements LinksStorage {

    private final ScrapperClient scrapperClient;

    @Override
    public String registerUser(Long chatId) {
        var response = scrapperClient.registerChat(chatId);
        if (response.getStatusCode().isError()) {
            log.atWarn()
                    .setMessage("Error response while trying to register user.")
                    .addKeyValue("statusCode", response.getStatusCode())
                    .addKeyValue("message", response.getBody())
                    .addKeyValue("chatId", chatId)
                    .log();
            return Responses.REGISTER_USER_FAIL.message;
        }
        return Responses.REGISTER_USER_SUCCESS.message;
    }

    @Override
    public String addUserLink(Long chatId, String url, List<String> tags, List<String> filters) {
        var response = scrapperClient.addLink(chatId, new AddLinkRequest(URI.create(url), tags, filters));
        if (response.isError()) {
            log.atWarn()
                    .setMessage("Error response while trying to add user link.")
                    .addKeyValue("statusCode", response.apiErrorResponse().code())
                    .addKeyValue("message", response.apiErrorResponse().exceptionMessage())
                    .addKeyValue("chatId", chatId)
                    .log();
            return response.apiErrorResponse().description();
        }
        return Responses.ADD_USER_LINK_SUCCESS.message;
    }

    @Override
    public String removeUserLink(Long chatId, String url) {
        try {
            var response = scrapperClient.removeLink(chatId, new RemoveLinkRequest(URI.create(url)));
            if (response.isError()) {
                log.atWarn()
                        .setMessage("Error response while trying to remove user link.")
                        .addKeyValue("statusCode", response.apiErrorResponse().code())
                        .addKeyValue("message", response.apiErrorResponse().exceptionMessage())
                        .addKeyValue("chatId", chatId)
                        .log();
                return response.apiErrorResponse().description();
            }
            return Responses.REMOVE_USER_LINK_SUCCESS.message;
        } catch (IllegalArgumentException e) {
            return Responses.REMOVE_USER_LINK_FAIL.message;
        }
    }

    @Override
    public List<Link> getLinks(Long chatId) {
        var response = scrapperClient.listLinks(chatId).answer();
        var linkDTOs = response.links();
        List<Link> links = new ArrayList<>();
        if (linkDTOs == null) {
            log.atWarn()
                    .setMessage("Error response while trying get user link.")
                    .addKeyValue("chatId", chatId)
                    .log();
            return links;
        }
        for (var link : linkDTOs) {
            links.add(new Link(link.url().toString()));
        }
        return links;
    }
}
