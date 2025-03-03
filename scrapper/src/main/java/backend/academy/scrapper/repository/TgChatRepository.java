package backend.academy.scrapper.repository;

import java.util.List;

public interface TgChatRepository {

    List<Long> findAll();

    void add(long chatId);

    void remove(long chatId);

    boolean isExists(long chatId);

}
