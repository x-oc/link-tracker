package backend.academy.bot.model;

import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

@Getter
@Setter
@RequiredArgsConstructor
public class Link {

    @URL
    private final String url;

    private List<String> tags;
    private List<String> filters;
}
