package backend.academy.bot.command;

import backend.academy.bot.model.CommandArguments;
import backend.academy.bot.model.Link;
import backend.academy.bot.service.LinksStorage;
import backend.academy.bot.service.RemoteLinksStorage;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class ListCommandTest {

    @DisplayName("Тестирование метода ListCommand#handle с пустым списком")
    @Test
    public void handleShouldReturnEmptyListMessageWhenListIsEmpty() {
        LinksStorage mockedLinksStorage = Mockito.mock(RemoteLinksStorage.class);
        Mockito.when(mockedLinksStorage.getLinks(1L)).thenReturn(List.of());
        ListCommand command = new ListCommand(mockedLinksStorage, null, null);

        Assertions.assertThat(command.handle(new CommandArguments("", 1L))).isEqualTo("No tracked links found");
        Mockito.verify(mockedLinksStorage, Mockito.times(1)).getLinks(1L);
    }

    @DisplayName("Тестирование метода ListCommand#handle с непустым списком")
    @Test
    public void handleShouldReturnListMessageWhenListIsNotEmpty() {
        LinksStorage mockedLinksStorage = Mockito.mock(RemoteLinksStorage.class);
        List<Link> links = List.of(new Link("link1"), new Link("link2"));
        Mockito.when(mockedLinksStorage.getLinks(1L)).thenReturn(links);
        ListCommand command = new ListCommand(mockedLinksStorage, null, null);

        String correctResponse = "Here's list of links that you are tracking now: \n link1\n link2";
        Assertions.assertThat(command.handle(new CommandArguments("", 1L))).isEqualTo(correctResponse);
        Mockito.verify(mockedLinksStorage, Mockito.times(1)).getLinks(1L);
    }
}
