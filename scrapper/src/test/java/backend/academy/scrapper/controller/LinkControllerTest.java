package backend.academy.scrapper.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import backend.academy.scrapper.dto.request.AddLinkRequest;
import backend.academy.scrapper.dto.request.RemoveLinkRequest;
import backend.academy.scrapper.dto.response.ApiErrorResponse;
import backend.academy.scrapper.dto.response.LinkResponse;
import backend.academy.scrapper.dto.response.ListLinksResponse;
import backend.academy.scrapper.exception.ChatNotFoundException;
import backend.academy.scrapper.exception.LinkNotSupportedException;
import backend.academy.scrapper.service.LinkService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@ActiveProfiles("test")
@WebMvcTest(LinkController.class)
@Import(LinkControllerTest.TestConfig.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LinkControllerTest {

    private static Long chatId;

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final LinkService linkService;

    @BeforeAll
    public static void setUpChatId() {
        chatId = 1L;
    }

    @AfterEach
    public void setNewChatId() {
        chatId += 1;
    }

    @Test
    @DisplayName("Тестирование LinkController#listLinks")
    public void listLinksShouldWorkCorrectly() throws Exception {
        Mockito.when(linkService.listLinks(chatId))
                .thenReturn(new ListLinksResponse(
                        List.of(new LinkResponse(chatId, URI.create("http://localhost"), null, null)), 1));
        mockMvc.perform(MockMvcRequestBuilders.get("/links")
                        .header("Tg-Chat-Id", chatId)
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(result -> Assertions.assertThat(result.getResponse().getContentAsString())
                        .isEqualTo(("{\"links\":[{\"id\":%s,\"url\":\"http://localhost\","
                                        + "\"tags\":null,\"filters\":null}],\"size\":1}")
                                .formatted(chatId)));
        Mockito.verify(linkService).listLinks(chatId);
    }

    @Test
    @DisplayName("Тестирование LinkController#listLinks с отсутвующим чатом")
    public void listLinksShouldReturnErrorWhenChatIsMissing() throws Exception {
        Mockito.when(linkService.listLinks(chatId)).thenThrow(new ChatNotFoundException(chatId));
        var result = mockMvc.perform(MockMvcRequestBuilders.get("/links")
                        .contentType("application/json")
                        .header("Tg-Chat-Id", chatId))
                .andExpect(status().isNotFound())
                .andReturn();

        ApiErrorResponse error =
                objectMapper.readValue(result.getResponse().getContentAsString(), ApiErrorResponse.class);
        Assertions.assertThat(error).extracting("code", "exceptionName").contains("404", "ChatNotFoundException");
    }

    @Test
    @DisplayName("Тестирование LinkController#listLinks без заголовка Tg-Chat-Id")
    public void listLinksShouldReturnErrorWhenTgChatIdIsMissing() throws Exception {
        var result = mockMvc.perform(MockMvcRequestBuilders.get("/links").contentType("application/json"))
                .andExpect(status().isBadRequest())
                .andReturn();

        ApiErrorResponse error =
                objectMapper.readValue(result.getResponse().getContentAsString(), ApiErrorResponse.class);
        Assertions.assertThat(error)
                .extracting("code", "exceptionName")
                .contains("400", "MissingRequestHeaderException");
    }

    @Test
    @DisplayName("Тестирование LinkController#addLink")
    public void addLinkShouldWorkCorrectly() throws Exception {
        Mockito.when(linkService.addLink(URI.create("http://localhost"), chatId, List.of(), List.of()))
                .thenReturn(new LinkResponse(chatId, URI.create("http://localhost"), null, null));
        mockMvc.perform(MockMvcRequestBuilders.post("/links")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                new AddLinkRequest(URI.create("http://localhost"), List.of(), List.of())))
                        .header("Tg-Chat-Id", chatId))
                .andExpect(status().isOk())
                .andExpect(result -> Assertions.assertThat(result.getResponse().getContentAsString())
                        .isEqualTo("{\"id\":%s,\"url\":\"http://localhost\",\"tags\":null,\"filters\":null}"
                                .formatted(chatId)));

        Mockito.verify(linkService).addLink(URI.create("http://localhost"), chatId, List.of(), List.of());
    }

    @Test
    @DisplayName("Тестирование LinkController#addLink с неверными данными")
    public void addLinkShouldReturnErrorWhenDataIsInvalid() throws Exception {
        var result = mockMvc.perform(MockMvcRequestBuilders.post("/links")
                        .contentType("application/json")
                        .content("{}")
                        .header("Tg-Chat-Id", chatId))
                .andExpect(status().isBadRequest())
                .andReturn();

        ApiErrorResponse error =
                objectMapper.readValue(result.getResponse().getContentAsString(), ApiErrorResponse.class);
        Assertions.assertThat(error)
                .extracting("code", "exceptionName")
                .contains("400", "MethodArgumentNotValidException");
    }

    @Test
    @DisplayName("Тестирование LinkController#addLink с неподдерживаемой ссылкой")
    public void addLinkShouldReturnErrorWhenLinkIsNotSupported() throws Exception {
        var uri = URI.create("http://localhost");
        Mockito.when(linkService.addLink(uri, chatId, List.of(), List.of()))
                .thenThrow(new LinkNotSupportedException("http://localhost"));
        var result = mockMvc.perform(MockMvcRequestBuilders.post("/links")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new AddLinkRequest(uri, List.of(), List.of())))
                        .header("Tg-Chat-Id", chatId))
                .andExpect(status().isBadRequest())
                .andReturn();

        ApiErrorResponse error =
                objectMapper.readValue(result.getResponse().getContentAsString(), ApiErrorResponse.class);
        Assertions.assertThat(error).extracting("code", "exceptionName").contains("400", "LinkNotSupportedException");
    }

    @Test
    @DisplayName("Тестирование LinkController#removeLink")
    public void removeLinkShouldWorkCorrectly() throws Exception {
        URI url = URI.create("http://localhost");
        mockMvc.perform(MockMvcRequestBuilders.delete("/links")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new RemoveLinkRequest(url)))
                        .header("Tg-Chat-Id", chatId))
                .andExpect(status().isOk());

        Mockito.verify(linkService).removeLink(url.toString(), chatId);
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        public LinkService linkService() {
            return Mockito.mock(LinkService.class);
        }
    }
}
