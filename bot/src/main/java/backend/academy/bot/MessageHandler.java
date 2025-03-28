package backend.academy.bot;

import backend.academy.bot.command.Command;
import backend.academy.bot.model.CommandArguments;
import backend.academy.bot.stateMachine.UserState;
import backend.academy.bot.stateMachine.UserStateHandler;
import backend.academy.bot.stateMachine.UserStateStorage;
import com.pengrad.telegrambot.model.LinkPreviewOptions;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessageHandler {

    @Getter
    private final List<Command> commands;

    private Map<String, Command> commandMap;
    private final UserStateStorage userStateStorage;
    private final UserStateHandler userStateHandler;

    @PostConstruct
    public void initCommandMap() {
        commandMap = commands.stream().collect(Collectors.toMap(Command::command, command -> command));
    }

    public SendMessage handle(Update update) {
        Long chatId = update.message().chat().id();
        String inputString = update.message().text();
        if (inputString == null) {
            return new SendMessage(chatId, "Command not found");
        }
        UserState userState = userStateStorage.getUserState(chatId);
        if (userState != null) {
            return userStateHandler.handleUserState(userState, chatId, inputString);
        }
        return handleCommand(chatId, inputString);
    }

    private SendMessage handleCommand(Long chatId, String inputString) {
        String inputCommand = inputString.split(" +")[0];
        String userArguments = inputString.contains(" ") ? inputString.substring(inputString.indexOf(" ") + 1) : "";
        CommandArguments commandArguments = new CommandArguments(userArguments, chatId);

        Command command = commandMap.get(inputCommand);
        if (command != null) {
            return new SendMessage(chatId, command.handle(commandArguments))
                    .linkPreviewOptions(new LinkPreviewOptions().isDisabled(true));
        }
        return new SendMessage(chatId, "Command not found");
    }
}
