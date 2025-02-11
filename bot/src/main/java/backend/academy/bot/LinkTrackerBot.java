package backend.academy.bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.TelegramException;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@RequiredArgsConstructor
public class LinkTrackerBot {

    private final TelegramBot bot;
    private final MessageHandler messageHandler;

    @PostConstruct
    public void start() {
        bot.setUpdatesListener(this::listenRequests, this::handleError);
    }

    private int listenRequests(List<Update> updates) {
        updates.forEach(update -> {
            if (update.message() != null) {
                SendMessage sendMessage = messageHandler.handle(update);
                if (sendMessage != null) {
                    bot.execute(sendMessage);
                }
            }
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private void handleError(TelegramException e) {
        if (e.response() != null) {
            e.response().errorCode();
            e.response().description();
        } else {
            e.printStackTrace();
        }
    }

    @PreDestroy
    private void close() {
        bot.shutdown();
    }
}
