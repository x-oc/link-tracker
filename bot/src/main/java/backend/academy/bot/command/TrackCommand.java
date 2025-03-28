package backend.academy.bot.command;

import backend.academy.bot.model.CommandArguments;
import backend.academy.bot.model.Link;
import backend.academy.bot.response.BotResponses;
import backend.academy.bot.service.LinksStorage;
import backend.academy.bot.stateMachine.UserState;
import backend.academy.bot.stateMachine.UserStateStorage;
import backend.academy.bot.validator.LinkValidator;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TrackCommand implements Command {

    private final LinksStorage linksStorage;
    private final UserStateStorage stateStorage;

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
        UserState userState = stateStorage.getUserState(arguments.chatId());
        return switch (userState) {
            case AWAITING_FILTERS -> handleAddFilters(arguments);
            case AWAITING_TAGS -> handleAddTags(arguments);
            case null -> trackNewLink(arguments);
        };
    }

    private String trackNewLink(CommandArguments arguments) {
        Link link = new Link(arguments.userArguments());

        if (!LinkValidator.isValid(link)) {
            return "Invalid link";
        }
        String response = linksStorage.addUserLink(arguments.chatId(), arguments.userArguments(), null, null);
        if (!response.equals(BotResponses.ADD_USER_LINK_SUCCESS.message)) {
            return response;
        }

        stateStorage.setUserState(arguments.chatId(), UserState.AWAITING_TAGS);
        stateStorage.setLastLink(arguments.chatId(), link);
        return "Enter tags for the link, please (or 'none' to skip): ";
    }

    private String handleAddTags(CommandArguments arguments) {
        List<String> tags = Arrays.stream(arguments.userArguments().split(" +")).toList();
        Link lastLink = stateStorage.getLastLink(arguments.chatId());
        if (lastLink == null) {
            return "Link not found!";
        }
        if (tags.isEmpty()) {
            return "Wrong command arguments!";
        }
        lastLink.tags(tags);
        stateStorage.setLastLink(arguments.chatId(), lastLink);
        boolean result = true;
        if (!Objects.equals(tags.getFirst().toLowerCase(), "none")) {
            result &= linksStorage
                    .removeUserLink(arguments.chatId(), lastLink.url())
                    .equals(BotResponses.REMOVE_USER_LINK_SUCCESS.message);
            result &= linksStorage
                    .addUserLink(arguments.chatId(), lastLink.url(), tags, null)
                    .equals(BotResponses.ADD_USER_LINK_SUCCESS.message);
        }
        if (!result) {
            stateStorage.clearUserState(arguments.chatId());
            return "Something went wrong!";
        }
        stateStorage.setUserState(arguments.chatId(), UserState.AWAITING_FILTERS);
        return "Now enter filters, please (or 'none'): ";
    }

    private String handleAddFilters(CommandArguments arguments) {
        List<String> filters =
                Arrays.stream(arguments.userArguments().split(" +")).toList();
        Link lastLink = stateStorage.getLastLink(arguments.chatId());
        if (lastLink == null) {
            return "Link not found!";
        }
        if (filters.isEmpty()) {
            return "Wrong command arguments!";
        }
        boolean result = true;
        if (!Objects.equals(filters.getFirst().toLowerCase(), "none")) {
            result &= linksStorage
                    .removeUserLink(arguments.chatId(), lastLink.url())
                    .equals(BotResponses.REMOVE_USER_LINK_SUCCESS.message);
            result &= linksStorage
                    .addUserLink(arguments.chatId(), lastLink.url(), lastLink.tags(), filters)
                    .equals(BotResponses.ADD_USER_LINK_SUCCESS.message);
        }
        stateStorage.clearUserState(arguments.chatId());
        if (!result) {
            return "Something went wrong!";
        }
        return String.format(
                "You started tracking the link %s! You will get a notification on its' update.", lastLink.url());
    }
}
