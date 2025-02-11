package backend.academy.bot.command;

import org.springframework.stereotype.Component;

@Component
public class ListCommand implements Command {

    @Override
    public String command() {
        return "/list";
    }

    @Override
    public String description() {
        return "show tracked links";
    }

    @Override
    public String handle(String input) {
        return "Here's list of links that you are tracking now: ...";
    }
}
