package backend.academy.scrapper.sender;

import backend.academy.scrapper.client.BotClient;
import backend.academy.scrapper.dto.request.LinkUpdate;
import backend.academy.scrapper.service.RetryAndCircuitBreakerService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.message-transport", havingValue = "HTTP")
@Service
public class HttpLinkUpdateSender implements LinkUpdateSender {

    private final BotClient botClient;
    private final RetryAndCircuitBreakerService retryAndCircuitBreakerService;

    @Override
    public void sendUpdate(LinkUpdate linkUpdate) {
        retryAndCircuitBreakerService.sendUpdateWithRetry(botClient::handleUpdates, linkUpdate);
    }
}
