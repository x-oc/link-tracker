package backend.academy.bot.service;

import backend.academy.bot.model.Link;
import java.util.List;

public interface LinksStorage {

    String registerUser(Long id);

    String addUserLink(Long userId, String url, List<String> tags, List<String> filters);

    String removeUserLink(Long userId, String url);

    List<Link> getLinks(Long userId);

    enum Responses {
        REGISTER_USER_SUCCESS("You started the bot! Type /help to see available commands."),
        REGISTER_USER_FAIL("An error occurred while trying to register. Sorry("),
        ADD_USER_LINK_SUCCESS("Link added successfully!"),
        REMOVE_USER_LINK_SUCCESS("Link removed successfully!"),
        REMOVE_USER_LINK_FAIL("Invalid link!");

        public final String message;

        Responses(String message) {
            this.message = message;
        }
    }
}
