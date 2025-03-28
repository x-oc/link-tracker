package backend.academy.bot.stateMachine;

import backend.academy.bot.command.TrackCommand;
import backend.academy.bot.model.CommandArguments;
import com.pengrad.telegrambot.model.LinkPreviewOptions;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
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
        log.atWarn()
                .setMessage("Unknown user state.")
                .addKeyValue("chatId", chatId)
                .addKeyValue("userState", state.name())
                .log();
        return new SendMessage(chatId, "Something went wrong");
    }
}
