package backend.academy.scrapper.repository;

import backend.academy.scrapper.model.Link;
import java.util.List;

public interface TgChatLinkRepository {

    void add(long chatId, long linkId);

    void remove(long chatId, long linkId);

    List<Link> findAllByChatId(long chatId);

    List<Long> findAllByLinkId(long linkId);

    void removeAllByChatId(Long chatId);

    boolean isExists(long chatId, long linkId);
}
