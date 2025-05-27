package backend.academy.bot.command;

import backend.academy.bot.model.CommandArguments;

public interface Command {

    String command();

    default String commandWithArguments() {
        return command();
    }

    String description();

    String handle(CommandArguments arguments);
}
