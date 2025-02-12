package backend.academy.bot.command;

import backend.academy.bot.model.CommandArguments;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@RequiredArgsConstructor
public class HelpCommand implements Command {

    private final List<Command> commands;

    @Override
    public String command() {
        return "/help";
    }

    @Override
    public String description() {
        return "show available commands";
    }

    @Override
    public String handle(CommandArguments arguments) {
        StringBuilder sb = new StringBuilder("Sure! Here's the list of available commands: \n ");
        commands.forEach(command -> sb
            .append(command.command())
            .append(" ")
            .append(command.description())
            .append("\n "));
        return sb.toString();
    }
}
