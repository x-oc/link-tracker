package backend.academy.scrapper.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ScrapperException extends RuntimeException {
    private final String description;
    private final HttpStatus status;

    public ScrapperException(String description, String message, HttpStatus status) {
        super(message);
        this.description = description;
        this.status = status;
    }
}
