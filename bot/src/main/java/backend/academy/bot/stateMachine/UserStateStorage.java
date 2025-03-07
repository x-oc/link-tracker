package backend.academy.bot.stateMachine;

import backend.academy.bot.model.Link;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class UserStateStorage {

    private final Map<Long, UserState> userStates = new HashMap<>();
    private final Map<Long, Link> lastLinks = new HashMap<>();

    public void setUserState(long chatId, UserState state) {
        userStates.put(chatId, state);
    }

    public UserState getUserState(long chatId) {
        return userStates.get(chatId);
    }

    public void clearUserState(long chatId) {
        userStates.remove(chatId);
    }

    public void setLastLink(long chatId, Link link) {
        lastLinks.put(chatId, link);
    }

    public Link getLastLink(long chatId) {
        return lastLinks.get(chatId);
    }
}
