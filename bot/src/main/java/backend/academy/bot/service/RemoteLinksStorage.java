package backend.academy.bot.service;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.dto.request.AddLinkRequest;
import backend.academy.bot.dto.request.RemoveLinkRequest;
import backend.academy.bot.exception.ApiErrorException;
import backend.academy.bot.model.Link;
import backend.academy.bot.response.BotResponses;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@Service
@RequiredArgsConstructor
public class RemoteLinksStorage implements LinksStorage {

    private final ScrapperClient scrapperClient;

    @Override
    public String registerUser(Long chatId) {
        try {
            scrapperClient.registerChat(chatId);
            return BotResponses.REGISTER_USER_SUCCESS.message;
        } catch (Exception e) {
            String response = handleScrapperClientException(e, chatId, "registerUser");
            if (response == null || response.isEmpty() || response.equals(BotResponses.FAIL.message)) {
                return BotResponses.REGISTER_USER_FAIL.message;
            }
            return response;
        }
    }

    @Override
    public String addUserLink(Long chatId, String url, List<String> tags, List<String> filters) {
        try {
            scrapperClient.addLink(chatId, new AddLinkRequest(URI.create(url), tags, filters));
            return BotResponses.ADD_USER_LINK_SUCCESS.message;
        } catch (Exception e) {
            return handleScrapperClientException(e, chatId, "addUserLink");
        }
    }

    @Override
    public String removeUserLink(Long chatId, String url) {
        try {
            scrapperClient.removeLink(chatId, new RemoveLinkRequest(URI.create(url)));
            return BotResponses.REMOVE_USER_LINK_SUCCESS.message;
        } catch (Exception e) {
            String response = handleScrapperClientException(e, chatId, "removeUserLink");
            if (response == null || response.isEmpty() || response.equals(BotResponses.FAIL.message)) {
                return BotResponses.REMOVE_USER_LINK_FAIL.message;
            }
            return response;
        }
    }

    @Override
    public List<Link> getLinks(Long chatId) {
        List<Link> links = new ArrayList<>();
        try {
            var response = scrapperClient.listLinks(chatId).answer();
            var linkDTOs = response.links();
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
        } catch (Exception e) {
            return links;
        }
    }

    private String handleScrapperClientException(Exception e, Long chatId, String action) {
        if (e instanceof WebClientResponseException webClientResponseException) {
            log.atWarn()
                .setMessage("Error while getting response from scrapper.")
                .addKeyValue("action", action)
                .addKeyValue("statusCode", webClientResponseException.getStatusCode())
                .addKeyValue("message", webClientResponseException.getResponseBodyAsString())
                .addKeyValue("chatId", chatId)
                .log();
            return webClientResponseException.getResponseBodyAsString();
        } else if (e instanceof ApiErrorException apiErrorException) {
            log.atWarn()
                .setMessage("Error response from scrapper.")
                .addKeyValue("action", action)
                .addKeyValue("statusCode", apiErrorException.getErrorResponse().code())
                .addKeyValue("message", apiErrorException.getErrorResponse().exceptionMessage())
                .addKeyValue("chatId", chatId)
                .log();
            return apiErrorException.getErrorResponse().exceptionMessage();
        } else {
            log.atWarn()
                .setMessage("Unknown error while getting response from scrapper.")
                .addKeyValue("action", action)
                .addKeyValue("stackTrace", e.getStackTrace())
                .addKeyValue("message", e.getMessage())
                .addKeyValue("chatId", chatId)
                .log();
            return BotResponses.FAIL.message;
        }
    }
}
