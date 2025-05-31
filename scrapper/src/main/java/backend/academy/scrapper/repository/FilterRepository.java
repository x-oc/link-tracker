package backend.academy.scrapper.repository;

import java.util.List;

public interface FilterRepository {

    List<String> findByLink(long linkId);

    void add(long linkId, String filter);

    void removeByLink(long linkId);
}
