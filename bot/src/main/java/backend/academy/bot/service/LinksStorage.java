package backend.academy.bot.service;

import backend.academy.bot.model.Link;
import java.util.List;

public interface LinksStorage {

    void registerUser(Long id);

    boolean addUserLink(Long userId, String url);

    boolean removeUserLink(Long userId, String url);

    List<Link> getLinks(Long userId);
}
