package backend.academy.bot.command;

import org.springframework.stereotype.Component;

@Component
public class TrackCommand implements Command {

    @Override
    public String command() {
        return "/track";
    }

    @Override
    public String description() {
        return "start tracking the link";
    }

    @Override
    public String handle(String input) {
        return String.format("You started tracking the link %s! You will get a notification on its' update.", input);
    }
}
