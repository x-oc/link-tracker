package backend.academy.bot.response;

public enum BotResponses {
    REGISTER_USER_SUCCESS("You started the bot! Type /help to see available commands."),
    REGISTER_USER_FAIL("An error occurred while trying to register. Sorry("),
    ADD_USER_LINK_SUCCESS("Link added successfully!"),
    REMOVE_USER_LINK_SUCCESS("Link removed successfully!"),
    REMOVE_USER_LINK_FAIL("Invalid link!");

    public final String message;

    BotResponses(String message) {
        this.message = message;
    }
}
