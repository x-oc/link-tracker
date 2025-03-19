package backend.academy.bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.TelegramException;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SetMyCommands;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LinkTrackerBot {

    private final TelegramBot bot;
    private final MessageHandler messageHandler;

    @PostConstruct
    public void start() {
        bot.execute(new SetMyCommands(messageHandler.commands().stream()
                .map(command -> new BotCommand(command.command(), command.description()))
                .toList()
                .toArray(new BotCommand[0])));
        bot.setUpdatesListener(this::listenRequests, this::handleError);
    }

    private int listenRequests(List<Update> updates) {
        updates.forEach(update -> {
            if (update.message() != null) {
                log.atInfo()
                        .setMessage("Processing new message.")
                        .addKeyValue("message", update.message())
                        .log();
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
            log.atError()
                    .setCause(e)
                    .addKeyValue("errorCode", e.response().errorCode())
                    .addKeyValue("description", e.response().description())
                    .log();
        } else {
            log.atError().setCause(e).log();
        }
    }

    @PreDestroy
    private void close() {
        bot.shutdown();
    }
}
