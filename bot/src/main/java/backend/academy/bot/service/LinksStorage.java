package backend.academy.bot.service;

import backend.academy.bot.model.Link;
import java.util.List;

public interface LinksStorage {

    String registerUser(Long id);

    String addUserLink(Long userId, String url, List<String> tags, List<String> filters);

    String removeUserLink(Long userId, String url);

    List<Link> getLinks(Long userId);
}
