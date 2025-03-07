package backend.academy.scrapper.exception;

import java.net.URI;
import org.springframework.http.HttpStatus;

public class LinkAlreadyAddedException extends ScrapperException {
    public LinkAlreadyAddedException(URI link) {
        super("Link %s is already added".formatted(link.toString()), "Link already added", HttpStatus.BAD_REQUEST);
    }
}
