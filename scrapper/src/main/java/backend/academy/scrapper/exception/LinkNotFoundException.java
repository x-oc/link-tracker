package backend.academy.scrapper.exception;

import org.springframework.http.HttpStatus;

public class LinkNotFoundException extends ScrapperException {
    public LinkNotFoundException(String url) {
        super(String.format("Link %s not found", url), "Link not found", HttpStatus.NOT_FOUND);
    }
}
