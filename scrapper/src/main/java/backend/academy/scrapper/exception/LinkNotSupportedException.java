package backend.academy.scrapper.exception;

import java.net.URI;
import org.springframework.http.HttpStatus;

public class LinkNotSupportedException extends ScrapperException {

    public LinkNotSupportedException(String link) {
        super(
            "Link %s is not supported".formatted(link),
            "Link is not supported",
            HttpStatus.BAD_REQUEST
        );
    }
}
