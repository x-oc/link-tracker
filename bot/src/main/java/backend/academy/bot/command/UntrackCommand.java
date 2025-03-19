package backend.academy.bot.command;

import backend.academy.bot.model.CommandArguments;
import backend.academy.bot.response.BotResponses;
import backend.academy.bot.service.LinksStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UntrackCommand implements Command {

    private final LinksStorage linksStorage;

    @Override
    public String command() {
        return "/untrack";
    }

    @Override
    public String description() {
        return "stop tracking the link";
    }

    @Override
    public String handle(CommandArguments arguments) {
        String response = linksStorage.removeUserLink(arguments.chatId(), arguments.userArguments());
        if (!response.equals(BotResponses.REMOVE_USER_LINK_SUCCESS.message)) {
            return response;
        }
        return String.format(
                "You stopped tracking the link %s! You will no longer get notifications on its' updates.",
                arguments.userArguments());
    }
}
