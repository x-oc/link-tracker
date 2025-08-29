package backend.academy.scrapper.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import backend.academy.scrapper.config.ScrapperConfig;
import backend.academy.scrapper.dto.response.ApiErrorResponse;
import backend.academy.scrapper.exception.ChatAlreadyRegisteredException;
import backend.academy.scrapper.exception.ChatNotFoundException;
import backend.academy.scrapper.service.ChatService;
import backend.academy.scrapper.service.IpRateLimiterService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@ActiveProfiles("test")
@WebMvcTest(ChatController.class)
@TestPropertySource(
        properties = {
            "app.rate-limiter.timeout-duration=0s",
            "app.rate-limiter.limit-for-period=100",
            "app.rate-limiter.limit-refresh-period=5s"
        })
@Import(ChatControllerTest.TestConfig.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ChatControllerTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final IpRateLimiterService ipRateLimiterService;

    @MockitoBean
    private final ChatService chatService;

    @Test
    @DisplayName("Тестирование TelegramChatController#registerChat")
    public void registerChatShouldWorkCorrectly() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/tg-chat/1")).andExpect(status().isOk());

        Mockito.verify(chatService).registerChat(1L);
    }

    @Test
    @DisplayName("Тестирование TelegramChatController#registerChat при повторной регистрации")
    public void registerChatShouldReturnErrorWhenAlreadyRegistered() throws Exception {
        Mockito.doThrow(new ChatAlreadyRegisteredException(10L))
                .when(chatService)
                .registerChat(10L);
        var result = mockMvc.perform(MockMvcRequestBuilders.post("/tg-chat/10"))
                .andExpect(status().isBadRequest())
                .andReturn();

        ApiErrorResponse error =
                objectMapper.readValue(result.getResponse().getContentAsString(), ApiErrorResponse.class);
        Assertions.assertThat(error)
                .extracting("code", "exceptionName")
                .contains("400", "ChatAlreadyRegisteredException");
        Mockito.verify(chatService).registerChat(10L);
    }

    @Test
    @DisplayName("Тестирование TelegramChatController#deleteChat")
    public void deleteChatShouldWorkCorrectly() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/tg-chat/1")).andExpect(status().isOk());

        Mockito.verify(chatService).deleteChat(1L);
    }

    @Test
    @DisplayName("Тестирование TelegramChatController#deleteChat при некорректных данных")
    public void deleteChatShouldReturnError() throws Exception {
        Mockito.doThrow(new ChatNotFoundException(10L)).when(chatService).deleteChat(10L);
        mockMvc.perform(MockMvcRequestBuilders.delete("/tg-chat/10")).andExpect(status().isNotFound());

        Mockito.verify(chatService).deleteChat(10L);
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        public IpRateLimiterService ipRateLimiterService(ScrapperConfig config) {
            return new IpRateLimiterService(config);
        }
    }
}
