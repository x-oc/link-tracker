package backend.academy.bot.command;

import backend.academy.bot.model.CommandArguments;
import backend.academy.bot.service.LinksStorage;
import backend.academy.bot.service.RemoteLinksStorage;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class StartCommandTest {

    @DisplayName("Тестирование метода StartCommand#handle")
    @Test
    public void handleShouldRegisterUser() {
        LinksStorage mockedLinksStorage = Mockito.mock(RemoteLinksStorage.class);
        Mockito.when(mockedLinksStorage.registerUser(1L))
                .thenReturn(LinksStorage.Responses.REGISTER_USER_SUCCESS.message);
        StartCommand command = new StartCommand(mockedLinksStorage);

        Assertions.assertThat(command.handle(new CommandArguments("", 1L)))
                .isEqualTo((LinksStorage.Responses.REGISTER_USER_SUCCESS.message));
        Mockito.verify(mockedLinksStorage, Mockito.times(1)).registerUser(1L);
    }
}
