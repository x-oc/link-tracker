package backend.academy.bot.command;

import backend.academy.bot.model.CommandArguments;
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
        if (!linksStorage.removeUserLink(arguments.chatId(), arguments.userArguments())) {
           return String.format("Link '%s' not found", arguments.userArguments());
        }
        return String.format("You stopped tracking the link %s! " +
            "You will no longer get notifications on its' updates.", arguments.userArguments());
    }
}
