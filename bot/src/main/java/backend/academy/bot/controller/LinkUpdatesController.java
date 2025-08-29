package backend.academy.bot.controller;

import backend.academy.bot.dto.request.LinkUpdate;
import backend.academy.bot.service.IpRateLimiterService;
import backend.academy.bot.service.LinkNotificationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/updates", consumes = "application/json")
@RequiredArgsConstructor
public class LinkUpdatesController {

    private final LinkNotificationService linkNotificationService;
    private final IpRateLimiterService ipRateLimiterService;

    @PostMapping
    public void handleUpdates(@RequestBody @Valid LinkUpdate linkUpdate, HttpServletRequest request) {
        ipRateLimiterService.executeRateLimited(request.getRemoteAddr(), () -> {
            linkNotificationService.notifyLinkUpdate(linkUpdate);
            return null;
        });
    }
}
