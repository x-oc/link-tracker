package backend.academy.bot.client;

import backend.academy.bot.dto.OptionalAnswer;
import backend.academy.bot.dto.request.AddLinkRequest;
import backend.academy.bot.dto.request.RemoveLinkRequest;
import backend.academy.bot.dto.response.LinkResponse;
import backend.academy.bot.dto.response.ListLinksResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;

public interface ScrapperClient {

    @PostExchange("/tg-chat/{id}")
    ResponseEntity<Void> registerChat(@PathVariable Long id);

    @DeleteExchange("/tg-chat/{id}")
    ResponseEntity<Void> deleteChat(@PathVariable Long id);

    @GetExchange("/links")
    OptionalAnswer<ListLinksResponse> listLinks(@RequestHeader("Tg-Chat-Id") Long tgChatId);

    @PostExchange("/links")
    OptionalAnswer<LinkResponse> addLink(
        @RequestHeader("Tg-Chat-Id") Long tgChatId,
        @RequestBody AddLinkRequest addLinkRequest
    );

    @DeleteExchange("/links")
    OptionalAnswer<LinkResponse> removeLink(
        @RequestHeader("Tg-Chat-Id") Long tgChatId,
        @RequestBody RemoveLinkRequest removeLinkRequest
    );
}
