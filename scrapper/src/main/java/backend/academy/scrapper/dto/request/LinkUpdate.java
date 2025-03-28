package backend.academy.scrapper.dto.request;

import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.util.List;

public record LinkUpdate(
        @NotNull Long id, @NotNull URI url, @NotNull String description, @NotNull List<Long> tgChatIds) {}
