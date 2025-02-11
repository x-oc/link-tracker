package backend.academy.bot.command;

import org.springframework.stereotype.Component;

@Component
public class StartCommand implements Command {

    @Override
    public String command() {
        return "/start";
    }

    @Override
    public String description() {
        return "start using the bot";
    }

    @Override
    public String handle(String input) {
        return "You started the bot! Type /help to see available commands.";
    }
}
