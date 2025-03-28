package backend.academy.scrapper.client;

import backend.academy.scrapper.dto.OptionalAnswer;
import backend.academy.scrapper.dto.request.LinkUpdate;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;

public interface BotClient {

    @PostExchange("/updates")
    OptionalAnswer<Void> handleUpdates(@RequestBody @Valid LinkUpdate linkUpdate);
}
