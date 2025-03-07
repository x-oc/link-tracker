package backend.academy.bot.command;

import backend.academy.bot.model.CommandArguments;

public interface Command {

    String command();

    String description();

    String handle(CommandArguments arguments);
}
