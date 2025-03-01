package backend.academy.bot.command;

import backend.academy.bot.model.CommandArguments;
import backend.academy.bot.service.LinksStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TrackCommand implements Command {

    private final LinksStorage linksStorage;

    @Override
    public String command() {
        return "/track";
    }

    @Override
    public String description() {
        return "start tracking the link";
    }

    @Override
    public String handle(CommandArguments arguments) {
        if (!linksStorage.addUserLink(arguments.chatId(), arguments.userArguments(), null, null)) {
            return "Invalid link!";
        }
        return String.format("You started tracking the link %s! You will get a notification on its' update.",
            arguments.userArguments());
    }
}
