package backend.academy.bot.service;

import java.util.List;

public interface LinksStorage {

    void registerUser(Long id);

    boolean addUserLink(Long userId, String link);

    boolean removeUserLink(Long userId, String link);

    List<String> getLinks(Long userId);
}
