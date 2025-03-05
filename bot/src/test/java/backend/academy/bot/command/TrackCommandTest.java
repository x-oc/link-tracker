package backend.academy.bot.command;

import backend.academy.bot.TestApplication;
import backend.academy.bot.model.CommandArguments;
import backend.academy.bot.model.Link;
import backend.academy.bot.service.LinksStorage;
import backend.academy.bot.service.RemoteLinksStorage;
import backend.academy.bot.stateMachine.UserState;
import backend.academy.bot.stateMachine.UserStateStorage;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.List;

@SpringBootTest(classes = TestApplication.class)
public class TrackCommandTest {

    @Autowired
    private UserStateStorage stateStorage;

    @DisplayName("Тестирование метода TrackCommand#handle с валидными данными и ответом сервера")
    @Test
    public void handleShouldTrackLink() {
        String url = "https://github.com/user/repo";
        LinksStorage mockLinksStorage = Mockito.mock(RemoteLinksStorage.class);
        Mockito.when(mockLinksStorage.addUserLink(1L, url, null, null))
            .thenReturn(LinksStorage.Responses.ADD_USER_LINK_SUCCESS.message);
        UserStateStorage spiedStateStorage = Mockito.spy(stateStorage);
        TrackCommand command = new TrackCommand(mockLinksStorage, spiedStateStorage);

        Assertions.assertThat(command.handle(new CommandArguments(url, 1L)))
            .isEqualTo("Enter tags for the link, please (or 'none' to skip): ");
        Mockito.verify(mockLinksStorage, Mockito.times(1))
            .addUserLink(1L, url, null, null);
        Mockito.verify(spiedStateStorage, Mockito.times(1))
            .setLastLink(Mockito.anyLong(), Mockito.any());
        Mockito.verify(spiedStateStorage, Mockito.times(1))
            .setUserState(1L, UserState.AWAITING_TAGS);
    }

    @DisplayName("Тестирование метода TrackCommand#handle с валидными данными и ошибкой сервера")
    @Test
    public void handleShouldReturnErrorWhenServerError() {
        String url = "https://example.com";
        LinksStorage mockLinksStorage = Mockito.mock(RemoteLinksStorage.class);
        Mockito.when(mockLinksStorage.addUserLink(2L, url, null, null))
            .thenReturn("Link %s is not supported".formatted(url));
        TrackCommand command = new TrackCommand(mockLinksStorage, stateStorage);

        Assertions.assertThat(command.handle(new CommandArguments(url, 2L)))
            .isEqualTo("Link %s is not supported".formatted(url));
        Mockito.verify(mockLinksStorage, Mockito.times(1))
            .addUserLink(2L, url, null, null);
    }

    @DisplayName("Тестирование метода TrackCommand#handle с невалидными данными")
    @Test
    public void handleShouldReturnErrorWhenInvalidUrl() {
        LinksStorage mockLinksStorage = Mockito.mock(RemoteLinksStorage.class);
        TrackCommand command = new TrackCommand(mockLinksStorage, stateStorage);

        Assertions.assertThat(command.handle(
                new CommandArguments("not even a link", 3L)))
            .isEqualTo("Invalid link");
        Mockito.verifyNoInteractions(mockLinksStorage);
    }

    @DisplayName("Тестирование метода TrackCommand#handle с вводом тегов")
    @Test
    public void handleShouldAddTags() {
        String url = "https://github.com/user/repo";
        LinksStorage mockLinksStorage = Mockito.mock(RemoteLinksStorage.class);
        Mockito.when(mockLinksStorage.removeUserLink(4L, url))
            .thenReturn(LinksStorage.Responses.REMOVE_USER_LINK_SUCCESS.message);
        List<String> tags = List.of("tag1", "tag2");
        Mockito.when(mockLinksStorage.addUserLink(4L, url, tags, null))
            .thenReturn(LinksStorage.Responses.ADD_USER_LINK_SUCCESS.message);

        UserStateStorage mockUserStateStorage = Mockito.mock(UserStateStorage.class);
        Mockito.when(mockUserStateStorage.getUserState(4L))
            .thenReturn(UserState.AWAITING_TAGS);
        Mockito.when(mockUserStateStorage.getLastLink(4L))
            .thenReturn(new Link(url));

        TrackCommand command = new TrackCommand(mockLinksStorage, mockUserStateStorage);

        Assertions.assertThat(command.handle(new CommandArguments("tag1 tag2", 4L)))
            .isEqualTo("Now enter filters, please (or 'none'): ");
        Mockito.verify(mockLinksStorage, Mockito.times(1))
            .addUserLink(4L, url, tags, null);
        Mockito.verify(mockLinksStorage, Mockito.times(1))
            .removeUserLink(4L, url);
        Mockito.verify(mockUserStateStorage, Mockito.times(1))
            .getLastLink(4L);
        Mockito.verify(mockUserStateStorage, Mockito.times(1))
            .getUserState(4L);
    }


    @DisplayName("Тестирование метода TrackCommand#handle с вводом фильтров")
    @Test
    public void handleShouldAddFilters() {
        String url = "https://github.com/user/repo";
        LinksStorage mockLinksStorage = Mockito.mock(RemoteLinksStorage.class);
        Mockito.when(mockLinksStorage.removeUserLink(5L, url))
            .thenReturn(LinksStorage.Responses.REMOVE_USER_LINK_SUCCESS.message);
        List<String> filters = List.of("filter1", "filter2");
        List<String> tags = List.of("tag1", "tag2");
        Mockito.when(mockLinksStorage.addUserLink(5L, url, tags, filters))
            .thenReturn(LinksStorage.Responses.ADD_USER_LINK_SUCCESS.message);

        UserStateStorage mockUserStateStorage = Mockito.mock(UserStateStorage.class);
        Mockito.when(mockUserStateStorage.getUserState(5L))
            .thenReturn(UserState.AWAITING_FILTERS);
        Link link = new Link(url);
        link.tags(tags);
        Mockito.when(mockUserStateStorage.getLastLink(5L))
            .thenReturn(link);

        TrackCommand command = new TrackCommand(mockLinksStorage, mockUserStateStorage);

        Assertions.assertThat(command.handle(new CommandArguments("filter1 filter2", 5L)))
            .isEqualTo(("You started tracking the link %s!" +
                " You will get a notification on its' update.").formatted(url));
        Mockito.verify(mockLinksStorage, Mockito.times(1))
            .addUserLink(5L, url, tags, filters);
        Mockito.verify(mockLinksStorage, Mockito.times(1))
            .removeUserLink(5L, url);
        Mockito.verify(mockUserStateStorage, Mockito.times(1))
            .getLastLink(5L);
        Mockito.verify(mockUserStateStorage, Mockito.times(1))
            .getUserState(5L);
    }
}
