package backend.academy.bot.command;

import org.springframework.stereotype.Component;

@Component
public class HelpCommand implements Command {

    @Override
    public String command() {
        return "/help";
    }

    @Override
    public String description() {
        return "show available commands";
    }

    @Override
    public String handle(String input) {
        return "Sure! Here's the list of available commands: ...";
    }
}
