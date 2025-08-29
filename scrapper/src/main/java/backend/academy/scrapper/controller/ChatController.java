package backend.academy.scrapper.controller;

import backend.academy.scrapper.service.ChatService;
import backend.academy.scrapper.service.IpRateLimiterService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tg-chat/{id}")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final IpRateLimiterService ipRateLimiterService;

    @Operation(summary = "Зарегистрировать чат")
    @PostMapping
    public void registerChat(@PathVariable Long id, HttpServletRequest request) {
        ipRateLimiterService.executeRateLimited(request.getRemoteAddr(), () -> {
            chatService.registerChat(id);
            return null;
        });
    }

    @Operation(summary = "Удалить чат")
    @DeleteMapping
    public void deleteChat(@PathVariable Long id, HttpServletRequest request) {
        ipRateLimiterService.executeRateLimited(request.getRemoteAddr(), () -> {
            chatService.deleteChat(id);
            return null;
        });
    }
}
