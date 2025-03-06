package backend.academy.bot.command;

import backend.academy.bot.TestApplication;
import backend.academy.bot.model.CommandArguments;
import backend.academy.bot.model.Link;
import backend.academy.bot.service.LinksStorage;
import backend.academy.bot.service.RemoteLinksStorage;
import backend.academy.bot.stateMachine.UserState;
import backend.academy.bot.stateMachine.UserStateStorage;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.List;

@SpringBootTest(classes = TestApplication.class)
public class TrackCommandTest {

    private static Long chatId;

    @Autowired
    private UserStateStorage stateStorage;

    private final LinksStorage mockLinksStorage = Mockito.mock(RemoteLinksStorage.class);

    @BeforeAll
    public static void setUpChatId() {
        chatId = 1L;
    }

    @AfterEach
    public void setNewChatId() {
        chatId += 1;
    }

    @DisplayName("Тестирование метода TrackCommand#handle с валидными данными и ответом сервера")
    @Test
    public void handleShouldTrackLink() {
        String url = "https://github.com/user/repo";
        Mockito.when(mockLinksStorage.addUserLink(chatId, url, null, null))
            .thenReturn(LinksStorage.Responses.ADD_USER_LINK_SUCCESS.message);
        UserStateStorage spiedStateStorage = Mockito.spy(stateStorage);
        TrackCommand command = new TrackCommand(mockLinksStorage, spiedStateStorage);

        Assertions.assertThat(command.handle(new CommandArguments(url, chatId)))
            .isEqualTo("Enter tags for the link, please (or 'none' to skip): ");
        Mockito.verify(mockLinksStorage, Mockito.times(1))
            .addUserLink(chatId, url, null, null);
        Mockito.verify(spiedStateStorage, Mockito.times(1))
            .setLastLink(Mockito.anyLong(), Mockito.any());
        Mockito.verify(spiedStateStorage, Mockito.times(1))
            .setUserState(chatId, UserState.AWAITING_TAGS);
    }

    @DisplayName("Тестирование метода TrackCommand#handle с валидными данными и ошибкой сервера")
    @Test
    public void handleShouldReturnErrorWhenServerError() {
        String url = "https://example.com";
        Mockito.when(mockLinksStorage.addUserLink(chatId, url, null, null))
            .thenReturn("Link %s is not supported".formatted(url));
        TrackCommand command = new TrackCommand(mockLinksStorage, stateStorage);

        Assertions.assertThat(command.handle(new CommandArguments(url, chatId)))
            .isEqualTo("Link %s is not supported".formatted(url));
        Mockito.verify(mockLinksStorage, Mockito.times(1))
            .addUserLink(chatId, url, null, null);
    }

    @DisplayName("Тестирование метода TrackCommand#handle с невалидными данными")
    @Test
    public void handleShouldReturnErrorWhenInvalidUrl() {
        TrackCommand command = new TrackCommand(mockLinksStorage, stateStorage);

        Assertions.assertThat(command.handle(
                new CommandArguments("not even a link", chatId)))
            .isEqualTo("Invalid link");
        Mockito.verifyNoInteractions(mockLinksStorage);
    }

    @DisplayName("Тестирование метода TrackCommand#handle с вводом тегов")
    @Test
    public void handleShouldAddTags() {
        String url = "https://github.com/user/repo";
        Mockito.when(mockLinksStorage.removeUserLink(chatId, url))
            .thenReturn(LinksStorage.Responses.REMOVE_USER_LINK_SUCCESS.message);
        List<String> tags = List.of("tag1", "tag2");
        Mockito.when(mockLinksStorage.addUserLink(chatId, url, tags, null))
            .thenReturn(LinksStorage.Responses.ADD_USER_LINK_SUCCESS.message);

        UserStateStorage mockUserStateStorage = Mockito.mock(UserStateStorage.class);
        Mockito.when(mockUserStateStorage.getUserState(chatId))
            .thenReturn(UserState.AWAITING_TAGS);
        Mockito.when(mockUserStateStorage.getLastLink(chatId))
            .thenReturn(new Link(url));

        TrackCommand command = new TrackCommand(mockLinksStorage, mockUserStateStorage);

        Assertions.assertThat(command.handle(new CommandArguments("tag1 tag2", chatId)))
            .isEqualTo("Now enter filters, please (or 'none'): ");
        Mockito.verify(mockLinksStorage, Mockito.times(1))
            .addUserLink(chatId, url, tags, null);
        Mockito.verify(mockLinksStorage, Mockito.times(1))
            .removeUserLink(chatId, url);
        Mockito.verify(mockUserStateStorage, Mockito.times(1))
            .getLastLink(chatId);
        Mockito.verify(mockUserStateStorage, Mockito.times(1))
            .getUserState(chatId);
    }


    @DisplayName("Тестирование метода TrackCommand#handle с вводом фильтров")
    @Test
    public void handleShouldAddFilters() {
        String url = "https://github.com/user/repo";
        Mockito.when(mockLinksStorage.removeUserLink(chatId, url))
            .thenReturn(LinksStorage.Responses.REMOVE_USER_LINK_SUCCESS.message);
        List<String> filters = List.of("filter1", "filter2");
        List<String> tags = List.of("tag1", "tag2");
        Mockito.when(mockLinksStorage.addUserLink(chatId, url, tags, filters))
            .thenReturn(LinksStorage.Responses.ADD_USER_LINK_SUCCESS.message);

        UserStateStorage mockUserStateStorage = Mockito.mock(UserStateStorage.class);
        Mockito.when(mockUserStateStorage.getUserState(chatId))
            .thenReturn(UserState.AWAITING_FILTERS);
        Link link = new Link(url);
        link.tags(tags);
        Mockito.when(mockUserStateStorage.getLastLink(chatId))
            .thenReturn(link);

        TrackCommand command = new TrackCommand(mockLinksStorage, mockUserStateStorage);

        Assertions.assertThat(command.handle(new CommandArguments("filter1 filter2", chatId)))
            .isEqualTo(("You started tracking the link %s!" +
                " You will get a notification on its' update.").formatted(url));
        Mockito.verify(mockLinksStorage, Mockito.times(1))
            .addUserLink(chatId, url, tags, filters);
        Mockito.verify(mockLinksStorage, Mockito.times(1))
            .removeUserLink(chatId, url);
        Mockito.verify(mockUserStateStorage, Mockito.times(1))
            .getLastLink(chatId);
        Mockito.verify(mockUserStateStorage, Mockito.times(1))
            .getUserState(chatId);
    }
}
