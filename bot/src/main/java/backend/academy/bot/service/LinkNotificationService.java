package backend.academy.bot.service;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.dto.request.LinkUpdate;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LinkNotificationService {

    private final TelegramBot bot;
    private final ScrapperClient scrapperClient;

    public void notifyLinkUpdate(LinkUpdate link) {
        link.tgChatIds().forEach(chatId -> {
            String responseMessage = String.format("New update on %s: %s", link.url(), link.description());
            SendResponse response = bot.execute(
                new SendMessage(
                    chatId,
                    responseMessage
                )
            );
            if (response.message() == null) {
                scrapperClient.deleteChat(chatId);
            }
        });
    }
}
