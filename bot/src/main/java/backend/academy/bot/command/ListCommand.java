package backend.academy.bot.command;

import backend.academy.bot.config.ApplicationConfig;
import backend.academy.bot.model.CommandArguments;
import backend.academy.bot.model.Link;
import backend.academy.bot.service.LinksStorage;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ListCommand implements Command {

    private final LinksStorage linksStorage;
    private final ApplicationConfig config;
    private final RedisTemplate<String, String> redisTemplate;

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
        String cacheKey = getCacheKey(arguments.chatId());
        String cachedResponse = getCachedResponse(cacheKey, arguments.chatId());

        if (cachedResponse != null && !cachedResponse.isEmpty()) {
            return cachedResponse;
        }

        List<Link> links = linksStorage.getLinks(arguments.chatId());
        if (links == null || links.isEmpty()) {
            String response = "No tracked links found";
            saveToCache(cacheKey, arguments.chatId(), response);
            return response;
        }

        StringBuilder sb = new StringBuilder("Here's list of links that you are tracking now: \n ");
        for (Link link : links) {
            sb.append(link.url()).append("\n ");
        }
        String response = sb.toString().stripTrailing();

        saveToCache(cacheKey, arguments.chatId(), response);
        return response;
    }

    private String getCacheKey(Long chatId) {
        try {
            return config.redis().listCommandCachePrefix() + chatId;
        } catch (Exception e) {
            log.atWarn().setMessage("Failed to set cache key for List command").log();
            return "default";
        }
    }

    private String getCachedResponse(String cacheKey, Long chatId) {
        try {
            return redisTemplate.opsForValue().get(cacheKey);
        } catch (Exception e) {
            log.atWarn()
                    .setMessage("Failed to get cache for List command from Redis")
                    .addKeyValue("chatId", chatId)
                    .addKeyValue("exception", e.getMessage())
                    .log();
            return null;
        }
    }

    private void saveToCache(String cacheKey, Long chatId, String answer) {
        try {
            redisTemplate.opsForValue().set(cacheKey, answer, config.redis().cacheTtl());
        } catch (Exception e) {
            log.atWarn()
                    .setMessage("Failed to set cache for List command to Redis")
                    .addKeyValue("chatId", chatId)
                    .addKeyValue("exception", e.getMessage())
                    .log();
        }
    }
}
