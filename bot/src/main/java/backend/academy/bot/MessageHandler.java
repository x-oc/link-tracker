package backend.academy.bot;

import backend.academy.bot.command.Command;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MessageHandler {

    private final List<Command> commands;

    public SendMessage handle(Update update) {
        Long chatId = update.message().chat().id();
        String inputString = update.message().text();
        if (inputString == null) return null;
        String inputCommand = inputString.split(" ")[0];
        for (Command command : commands) {
            if (command.command().equals(inputCommand)) {
                String arguments = inputString.contains(" ") ?
                    inputString.substring(inputString.indexOf(" ") + 1) : "";
                return new SendMessage(chatId, command.handle(arguments));
            }
        }
        return new SendMessage(chatId, "Command not found");
    }
}
