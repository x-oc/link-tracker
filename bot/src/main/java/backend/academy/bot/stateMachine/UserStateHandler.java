package backend.academy.bot.stateMachine;

import backend.academy.bot.command.TrackCommand;
import backend.academy.bot.model.CommandArguments;
import com.pengrad.telegrambot.model.LinkPreviewOptions;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserStateHandler {

    private final TrackCommand trackCommand;

    public SendMessage handleUserState(UserState state, Long chatId, String inputString) {
        if (state == UserState.AWAITING_FILTERS) {
            CommandArguments commandArguments = new CommandArguments(inputString, chatId);
            return new SendMessage(chatId, trackCommand.handle(commandArguments))
                .linkPreviewOptions(new LinkPreviewOptions().isDisabled(true));
        }
        if (state == UserState.AWAITING_TAGS) {
            CommandArguments commandArguments = new CommandArguments(inputString, chatId);
            return new SendMessage(chatId, trackCommand.handle(commandArguments))
                .linkPreviewOptions(new LinkPreviewOptions().isDisabled(true));
        }
        return new SendMessage(chatId, "Something went wrong");
    }
}
