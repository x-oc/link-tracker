package backend.academy.bot.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
public class Link {

    private final String url;
    private List<String> tags;
    private List<String> filters;

}
