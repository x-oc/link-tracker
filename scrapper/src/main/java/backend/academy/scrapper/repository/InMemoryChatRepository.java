package backend.academy.scrapper.repository;

import org.springframework.stereotype.Component;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class InMemoryChatRepository implements TgChatRepository {

    private final Set<Long> chatIds = new HashSet<>();

    @Override
    public List<Long> findAll() {
        return chatIds.stream().toList();
    }

    @Override
    public void add(long chatId) {
        chatIds.add(chatId);
    }

    @Override
    public void remove(long chatId) {
        chatIds.remove(chatId);
    }

    @Override
    public boolean isExists(long chatId) {
        return chatIds.contains(chatId);
    }

}
