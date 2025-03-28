package backend.academy.bot.command;

import backend.academy.bot.model.CommandArguments;
import backend.academy.bot.model.Link;
import backend.academy.bot.service.LinksStorage;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ListCommand implements Command {

    private final LinksStorage linksStorage;

    @Override
    public String command() {
        return "/list";
    }

    @Override
    public String description() {
        return "show tracked links";
    }

    @Override
    public String handle(CommandArguments arguments) {
        List<Link> links = linksStorage.getLinks(arguments.chatId());
        if (links == null || links.isEmpty()) {
            return "No tracked links found";
        }
        StringBuilder sb = new StringBuilder("Here's list of links that you are tracking now: \n ");
        for (Link link : links) {
            sb.append(link.url()).append("\n ");
        }
        return sb.toString().stripTrailing();
    }
}
