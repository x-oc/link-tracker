package backend.academy.bot.command;

import backend.academy.bot.model.CommandArguments;
import backend.academy.bot.service.LinksStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StartCommand implements Command {

    private final LinksStorage linksStorage;

    @Override
    public String command() {
        return "/start";
    }

    @Override
    public String description() {
        return "start using the bot";
    }

    @Override
    public String handle(CommandArguments arguments) {
        linksStorage.registerUser(arguments.chatId());
        return "You started the bot! Type /help to see available commands.";
    }
}
