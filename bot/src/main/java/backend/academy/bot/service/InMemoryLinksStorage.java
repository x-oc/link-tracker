package backend.academy.bot.service;

import backend.academy.bot.model.Link;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InMemoryLinksStorage implements LinksStorage {

    private final Map<Long, List<Link>> links = new ConcurrentHashMap<>();

    @Override
    public void registerUser(Long userId) {
        links.put(userId, new ArrayList<>());
    }

    @Override
    public boolean addUserLink(Long userId, String url) {
        Link link = new Link(url);
        links.computeIfAbsent(userId, _ -> new ArrayList<>()).add(link);
        return true;
    }

    @Override
    public boolean removeUserLink(Long userId, String url) {
        boolean removed = false;
        List<Link> linkList = links.computeIfAbsent(userId, _ -> new ArrayList<>());
        Iterator<Link> iterator = linkList.iterator();
        while (iterator.hasNext()) {
            Link link = iterator.next();
            if (link.url().equals(url)) {
                iterator.remove();
                removed = true;
            }
        }
        return removed;
    }

    @Override
    public List<Link> getLinks(Long userId) {
        if (!links.containsKey(userId)) {
            return null;
        }
        return links.get(userId);
    }
}
