package backend.academy.scrapper.repository;

import backend.academy.scrapper.model.Link;
import java.util.List;

public interface TagRepository {

    List<String> findByLink(long linkId);

    List<Link> findByTag(String tag);

    void add(long linkId, String tag);

    void removeByLinkId(long linkId);

}
