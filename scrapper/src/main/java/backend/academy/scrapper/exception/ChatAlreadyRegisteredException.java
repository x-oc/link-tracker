package backend.academy.scrapper.exception;

import org.springframework.http.HttpStatus;

public class ChatAlreadyRegisteredException extends ScrapperException {
    public ChatAlreadyRegisteredException(long chatId) {
        super(
            "Chat %d is already registered".formatted(chatId),
            "Chat is already registered",
            HttpStatus.BAD_REQUEST);
    }
}
