package backend.academy.bot.command;

import backend.academy.bot.model.CommandArguments;
import backend.academy.bot.model.Link;
import backend.academy.bot.service.LinksStorage;
import backend.academy.bot.stateMachine.UserState;
import backend.academy.bot.stateMachine.UserStateStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class TrackCommand implements Command {

    private final LinksStorage linksStorage;
    private final UserStateStorage stateMachine;

    @Override
    public String command() {
        return "/track";
    }

    @Override
    public String description() {
        return "start tracking the link";
    }

    @Override
    public String handle(CommandArguments arguments) {
        UserState userState = stateMachine.getUserState(arguments.chatId());
        if (userState == UserState.AWAITING_FILTERS) {
            return handleAddFilters(arguments);
        }
        if (userState == UserState.AWAITING_TAGS) {
            return handleAddTags(arguments);
        }
        if (!arguments.userArguments().matches(Link.URL_PATTERN)) {
            return "Invalid link";
        }
        String response = linksStorage.addUserLink(
            arguments.chatId(), arguments.userArguments(), null, null);
        if (!response.equals(LinksStorage.Responses.ADD_USER_LINK_SUCCESS.message)) {
            return response;
        }

        stateMachine.setUserState(arguments.chatId(), UserState.AWAITING_TAGS);
        stateMachine.setLastLink(arguments.chatId(), new Link(arguments.userArguments()));
        return "Enter tags for the link, please (or 'none' to skip): ";
    }

    public String handleAddTags(CommandArguments arguments) {
        List<String> tags = Arrays.stream(arguments.userArguments().split(" +")).toList();
        Link lastLink = stateMachine.getLastLink(arguments.chatId());
        if (lastLink == null) {
            return "Link not found!";
        }
        if (tags.isEmpty()) {
            return "Wrong command arguments!";
        }
        boolean result = true;
        if (!Objects.equals(tags.getFirst().toLowerCase(), "none")) {
            result &= (linksStorage.removeUserLink(arguments.chatId(), lastLink.url())
                .equals(LinksStorage.Responses.REMOVE_USER_LINK_SUCCESS.message));
            result &= (linksStorage.addUserLink(arguments.chatId(), lastLink.url(), tags, null)
                .equals(LinksStorage.Responses.ADD_USER_LINK_SUCCESS.message));
        }
        if (!result) {
            stateMachine.clearUserState(arguments.chatId());
            return "Something went wrong!";
        }
        stateMachine.setUserState(arguments.chatId(), UserState.AWAITING_FILTERS);
        return "Now enter filters, please (or 'none'): ";
    }

    public String handleAddFilters(CommandArguments arguments) {
        List<String> filters = Arrays.stream(arguments.userArguments().split(" +")).toList();
        Link lastLink = stateMachine.getLastLink(arguments.chatId());
        if (lastLink == null) {
            return "Link not found!";
        }
        if (filters.isEmpty()) {
            return "Wrong command arguments!";
        }
        boolean result = true;
        if (!Objects.equals(filters.getFirst().toLowerCase(), "none")) {
            result &= (linksStorage.removeUserLink(arguments.chatId(), lastLink.url())
                .equals(LinksStorage.Responses.REMOVE_USER_LINK_SUCCESS.message));
            result &= (linksStorage.addUserLink(arguments.chatId(), lastLink.url(), lastLink.tags(), filters)
                .equals(LinksStorage.Responses.ADD_USER_LINK_SUCCESS.message));
        }
        stateMachine.clearUserState(arguments.chatId());
        if (!result) {
            return "Something went wrong!";
        }
        return String.format("You started tracking the link %s! You will get a notification on its' update.",
            lastLink.url());
    }
}
