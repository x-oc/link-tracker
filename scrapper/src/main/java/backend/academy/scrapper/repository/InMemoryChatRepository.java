package backend.academy.scrapper.repository;

import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class InMemoryChatRepository implements TgChatRepository {

    private List<Long> chatIds;

    @Override
    public List<Long> findAll() {
        return chatIds;
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
