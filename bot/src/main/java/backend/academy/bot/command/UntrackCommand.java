package backend.academy.bot.command;

import org.springframework.stereotype.Component;

@Component
public class UntrackCommand implements Command {

    @Override
    public String command() {
        return "/untrack";
    }

    @Override
    public String description() {
        return "stop tracking the link";
    }

    @Override
    public String handle(String input) {
        return String.format("You stopped tracking the link %s! " +
            "You will no longer get notifications on its' updates.", input);
    }
}
