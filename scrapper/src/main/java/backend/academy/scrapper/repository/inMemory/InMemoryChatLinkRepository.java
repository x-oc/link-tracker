package backend.academy.scrapper.repository.inMemory;

import backend.academy.scrapper.model.Link;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class InMemoryChatLinkRepository {

    private final Map<Long, List<String>> chatLinks = new ConcurrentHashMap<>();
    private final InMemoryLinkRepository linkRepository;

    public void add(long chatId, String url) {
        chatLinks.computeIfAbsent(chatId, Long -> new ArrayList<>()).add(url);
    }

    public void remove(long chatId, String url) {
        chatLinks.computeIfAbsent(chatId, Long -> new ArrayList<>()).remove(url);
    }

    public List<Link> findAllByChatId(long chatId) {
        List<String> urls = chatLinks.get(chatId);
        List<Link> links = new ArrayList<>();
        if (urls == null) {
            return links;
        }
        for (String url : urls) {
            Optional<Link> link = linkRepository.findByUrl(url);
            link.ifPresent(links::add);
        }
        return links;
    }

    public List<Long> findAllByUrl(String url) {
        List<Long> chatIds = new ArrayList<>();
        for (var entry : chatLinks.entrySet()) {
            if (entry.getValue().contains(url)) {
                chatIds.add(entry.getKey());
            }
        }
        return chatIds;
    }

    public void removeAllByChatId(Long chatId) {
        chatLinks.remove(chatId);
    }

    public boolean isExists(long chatId, String url) {
        return chatLinks.computeIfAbsent(chatId, Long -> new ArrayList<>()).contains(url);
    }
}
