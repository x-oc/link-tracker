package backend.academy.scrapper.resilience;

import backend.academy.scrapper.controller.ChatController;
import backend.academy.scrapper.repository.IntegrationEnvironment;
import backend.academy.scrapper.service.ChatService;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import jakarta.servlet.http.HttpServletRequest;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class RateLimiterTest extends IntegrationEnvironment {

    @MockitoBean
    private ChatService chatService;

    @Autowired
    private ChatController controller;

    @Test
    void registerChatTooManyRequestsShouldNotBePermitted() {
        final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getRemoteAddr()).thenReturn("123");

        boolean isThrownByRateLimiter = false;
        try {
            IntStream.range(0, 100).parallel().forEach(ignored -> controller.registerChat(123L, request));
        } catch (RequestNotPermitted ignored) {
            isThrownByRateLimiter = true;
        }

        Assertions.assertTrue(isThrownByRateLimiter);
    }

    @Test
    void registerChatFewRequestsShouldBePermitted() {
        final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getRemoteAddr()).thenReturn("456");

        boolean isThrownByRateLimiter = false;
        try {
            IntStream.range(0, 3).parallel().forEach(ignored -> controller.registerChat(456L, request));
        } catch (RequestNotPermitted ignored) {
            isThrownByRateLimiter = true;
        }

        Assertions.assertFalse(isThrownByRateLimiter);
    }

    @Test
    void registerChatDifferentIpsShouldBePermitted() {
        final int countOfRequests = 100;
        final HttpServletRequest[] mockServlets = new HttpServletRequest[countOfRequests];

        for (int i = 0; i < countOfRequests; i++) {
            mockServlets[i] = Mockito.mock(HttpServletRequest.class);
            Mockito.when(mockServlets[i].getRemoteAddr()).thenReturn(String.valueOf(i));
        }

        boolean isThrownByRateLimiter = false;
        try {
            IntStream.range(0, countOfRequests)
                    .parallel()
                    .forEach(i -> controller.registerChat((long) i, mockServlets[i]));
        } catch (RequestNotPermitted ignored) {
            isThrownByRateLimiter = true;
        }

        Assertions.assertFalse(isThrownByRateLimiter);
    }
}
