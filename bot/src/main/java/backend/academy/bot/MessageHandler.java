package backend.academy.bot;

import backend.academy.bot.command.Command;
import backend.academy.bot.model.CommandArguments;
import backend.academy.bot.stateMachine.UserState;
import backend.academy.bot.stateMachine.UserStateHandler;
import backend.academy.bot.stateMachine.UserStateStorage;
import com.pengrad.telegrambot.model.LinkPreviewOptions;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MessageHandler {

    private final List<Command> commands;
    private final UserStateStorage userStateStorage;
    private final UserStateHandler userStateHandler;

    public SendMessage handle(Update update) {
        Long chatId = update.message().chat().id();
        String inputString = update.message().text();
        if (inputString == null) return null;
        UserState userState = userStateStorage.getUserState(chatId);
        if (userState != null) {
            return userStateHandler.handleUserState(userState, chatId, inputString);
        }
        String inputCommand = inputString.split(" +")[0];
        String userArguments = inputString.contains(" ") ?
            inputString.substring(inputString.indexOf(" ") + 1) : "";
        CommandArguments commandArguments = new CommandArguments(userArguments, chatId);
        for (Command command : commands) {
            if (command.command().equals(inputCommand)) {
                return new SendMessage(chatId, command.handle(commandArguments))
                    .linkPreviewOptions(new LinkPreviewOptions().isDisabled(true));
            }
        }
        return new SendMessage(chatId, "Command not found");
    }
}
