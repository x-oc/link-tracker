package backend.academy.bot.service;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.dto.request.LinkUpdate;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LinkNotificationService {

    private final TelegramBot bot;
    private final ScrapperClient scrapperClient;

    public void notifyLinkUpdate(LinkUpdate link) {
        link.tgChatIds().forEach(chatId -> {
            log.atDebug().setMessage("Sending updates.")
                .addKeyValue("chatId", chatId)
                .addKeyValue("link", link.url())
                .log();
            String responseMessage = String.format("New update on %s: %s", link.url(), link.description());
            SendResponse response = bot.execute(
                new SendMessage(
                    chatId,
                    responseMessage
                )
            );
            if (response.message() == null) {
                log.atWarn().setMessage("Got no response. Deleting chat.")
                    .addKeyValue("chatId", chatId)
                    .log();
                scrapperClient.deleteChat(chatId);
            }
        });
    }
}
