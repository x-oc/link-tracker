package backend.academy.bot.service;

import backend.academy.bot.model.Filter;
import backend.academy.bot.model.Link;
import backend.academy.bot.model.Tag;
import java.util.List;

public interface LinksStorage {

    void registerUser(Long id);

    boolean addUserLink(Long userId, String url, List<Tag> tags, List<Filter> filters);

    boolean removeUserLink(Long userId, String url);

    List<Link> getLinks(Long userId);
}
