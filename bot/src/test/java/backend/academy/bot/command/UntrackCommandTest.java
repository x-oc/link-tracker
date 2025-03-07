package backend.academy.bot.command;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.model.CommandArguments;
import backend.academy.bot.service.LinksStorage;
import backend.academy.bot.service.RemoteLinksStorage;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class UntrackCommandTest {

    @DisplayName("Тестирование метода UntrackCommand#handle с некорректной ссылкой")
    @Test
    public void handleShouldReturnErrorWhenInvalidLink() {
        LinksStorage linksStorage = new RemoteLinksStorage(Mockito.mock(ScrapperClient.class));
        LinksStorage spiedLinksStorage = Mockito.spy(linksStorage);
        UntrackCommand command = new UntrackCommand(spiedLinksStorage);

        Assertions.assertThat(command.handle(new CommandArguments("not even a link", 1L)))
                .isEqualTo(LinksStorage.Responses.REMOVE_USER_LINK_FAIL.message);
        Mockito.verify(spiedLinksStorage, Mockito.times(1)).removeUserLink(1L, "not even a link");
    }

    @DisplayName("Тестирование метода UntrackCommand#handle с корректной ссылкой")
    @Test
    public void handleShouldUntrackLink() {
        String url = "https://github.com/user/repo";
        LinksStorage mockedLinksStorage = Mockito.mock(RemoteLinksStorage.class);
        Mockito.when(mockedLinksStorage.removeUserLink(1L, url))
                .thenReturn(LinksStorage.Responses.REMOVE_USER_LINK_SUCCESS.message);
        UntrackCommand command = new UntrackCommand(mockedLinksStorage);

        Assertions.assertThat(command.handle(new CommandArguments(url, 1L)))
                .isEqualTo(
                        ("You stopped tracking the link %s! " + "You will no longer get notifications on its' updates.")
                                .formatted(url));
        Mockito.verify(mockedLinksStorage, Mockito.times(1)).removeUserLink(1L, url);
    }
}
