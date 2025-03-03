package backend.academy.scrapper.repository;

import backend.academy.scrapper.model.Link;
import java.util.List;

public interface TgChatLinkRepository {

    void add(long chatId, String url);

    void remove(long chatId, String url);

    List<Link> findAllByChatId(long chatId);

    List<Long> findAllByUrl(String url);

    void removeAllByChatId(Long chatId);

    boolean isExists(long chatId, String url);
}
