package backend.academy.scrapper.repository;

import backend.academy.scrapper.model.Link;
import java.util.List;

public interface TagRepository {

    List<String> findByLinkAndChat(long linkId, long chatId);

    List<Link> findByTag(String tag);

    void add(long linkId, String tag, long chatId);

    void removeByLinkId(long linkId);
}
