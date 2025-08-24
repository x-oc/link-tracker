package backend.academy.bot.command;

import backend.academy.bot.config.ApplicationConfig;
import backend.academy.bot.model.CommandArguments;
import backend.academy.bot.model.Link;
import backend.academy.bot.response.BotResponses;
import backend.academy.bot.service.LinksStorage;
import backend.academy.bot.validator.LinkValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UntrackCommand implements Command {

    private final LinksStorage linksStorage;
    private final ApplicationConfig config;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public String command() {
        return "/untrack";
    }

    @Override
    public String commandWithArguments() {
        return "/untrack <link>";
    }

    @Override
    public String description() {
        return "stop tracking the link";
    }

    @Override
    public String handle(CommandArguments arguments) {
        if (!LinkValidator.isValid(new Link(arguments.userArguments()))) {
            return BotResponses.REMOVE_USER_LINK_FAIL.message;
        }
        String response = linksStorage.removeUserLink(arguments.chatId(), arguments.userArguments());
        if (!response.equals(BotResponses.REMOVE_USER_LINK_SUCCESS.message)) {
            return response;
        }
        invalidateCache(arguments.chatId());
        return String.format(
                "You stopped tracking the link %s! You will no longer get notifications on its' updates.",
                arguments.userArguments());
    }

    private void invalidateCache(long chatId) {
        String cacheKey = null;
        try {
            cacheKey = config.redis().listCommandCachePrefix() + chatId;
            redisTemplate.delete(cacheKey);
        } catch (Exception e) {
            log.atWarn()
                    .setMessage("Failed to invalidate cache in Untrack command")
                    .addKeyValue("key", cacheKey)
                    .setCause(e)
                    .log();
        }
    }
}
