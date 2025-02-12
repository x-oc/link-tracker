package backend.academy.bot.service;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InMemoryLinksStorage implements LinksStorage {

    private final Map<Long, List<String>> links = new ConcurrentHashMap<>();

    @Override
    public void registerUser(Long userId) {
        links.put(userId, new ArrayList<>());
    }

    @Override
    public boolean addUserLink(Long userId, String link) {
        links.computeIfAbsent(userId, _ -> new ArrayList<>()).add(link);
        return true;
    }

    @Override
    public boolean removeUserLink(Long userId, String link) {
        if (!links.computeIfAbsent(userId, _ -> new ArrayList<>()).contains(link)) {
            return false;
        }
        links.computeIfAbsent(userId, _ -> new ArrayList<>()).removeIf(userLink -> userLink.equals(link));
        return true;
    }

    @Override
    public List<String> getLinks(Long userId) {
        if (!links.containsKey(userId)) {
            return null;
        }
        return links.get(userId);
    }
}
