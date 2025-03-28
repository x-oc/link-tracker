package backend.academy.bot.command;

import backend.academy.bot.model.CommandArguments;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class HelpCommandTest {

    @DisplayName("Тестирование метода HelpCommand#handle")
    @Test
    public void handleShouldReturnHelpMessage() {
        HelpCommand command = new HelpCommand(List.of(createMockCommand()));
        CommandArguments arguments = new CommandArguments("", 1L);
        Assertions.assertThat(command.handle(arguments))
                .isEqualTo("Sure! Here's the list of available commands: \n /mock mock description\n ");
    }

    private Command createMockCommand() {
        Command mockCommand = Mockito.mock(Command.class);
        Mockito.when(mockCommand.command()).thenReturn("/mock");
        Mockito.when(mockCommand.description()).thenReturn("mock description");
        return mockCommand;
    }
}
