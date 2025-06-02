package backend.academy.scrapper.sender;

import backend.academy.scrapper.client.BotClient;
import backend.academy.scrapper.dto.request.LinkUpdate;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.message-transport", havingValue = "HTTP")
@Service
public class HttpLinkUpdateSender implements LinkUpdateSender {

    private final BotClient botClient;

    @Override
    public void sendUpdate(LinkUpdate linkUpdate) {
        botClient.handleUpdates(linkUpdate);
    }
}
