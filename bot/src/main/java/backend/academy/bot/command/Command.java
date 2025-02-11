package backend.academy.bot.command;

public interface Command {

    String command();

    String description();

    String handle(String input);

}
