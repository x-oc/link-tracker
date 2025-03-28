package backend.academy.bot;

import backend.academy.bot.command.Command;
import backend.academy.bot.stateMachine.UserStateHandler;
import backend.academy.bot.stateMachine.UserStateStorage;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = TestApplication.class)
public class MessageHandlerTest {

    @Autowired
    private UserStateStorage userStateStorage;

    @Autowired
    private UserStateHandler userStateHandler;

    private MessageHandler handler;

    @BeforeEach
    public void setUp() {
        handler = new MessageHandler(List.of(createMockCommand()), userStateStorage, userStateHandler);
        handler.initCommandMap();
    }

    @DisplayName("Тестирование метода handle с корректными командой и сообщением")
    @Test
    public void handleShouldExecuteCommand() {
        Assertions.assertThat(handler.handle(createMockUpdate("/mock", 1L))
                        .getParameters()
                        .get("text"))
                .isEqualTo("Mock message");
    }

    @DisplayName("Тестирование метода handle с некорректной командой")
    @Test
    public void processShouldReturnNullWhenMessageIsNull() {
        SendMessage message = handler.handle(createMockUpdate("/not_existing", 1L));
        Assertions.assertThat(message.getParameters().get("text")).isEqualTo("Command not found");
        Assertions.assertThat(message.getParameters().get("chat_id")).isEqualTo(1L);
    }

    private Command createMockCommand() {
        Command command = Mockito.mock(Command.class, Answers.CALLS_REAL_METHODS);
        Mockito.when(command.command()).thenReturn("/mock");
        Mockito.when(command.handle(Mockito.any())).thenReturn("Mock message");
        return command;
    }

    public static Update createMockUpdate(String text, Long chatId) {
        Update mockUpdate = Mockito.mock(Update.class);
        Message mockMessage = Mockito.mock(Message.class);
        Chat mockChat = Mockito.mock(Chat.class);
        Mockito.when(mockChat.id()).thenReturn(chatId);
        Mockito.when(mockMessage.text()).thenReturn(text);
        Mockito.when(mockUpdate.message()).thenReturn(mockMessage);
        Mockito.when(mockMessage.chat()).thenReturn(mockChat);
        return mockUpdate;
    }
}
