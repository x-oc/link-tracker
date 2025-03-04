package backend.academy.scrapper.exception;

import org.springframework.http.HttpStatus;

public class ChatNotFoundException extends ScrapperException {
    public ChatNotFoundException(long chatId) {
        super(
            "Chat %d not found".formatted(chatId),
            "Chat is not found",
            HttpStatus.NOT_FOUND);
    }
}
