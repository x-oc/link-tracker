package backend.academy.scrapper.service.jdbc;

import backend.academy.scrapper.exception.ChatAlreadyRegisteredException;
import backend.academy.scrapper.exception.ChatNotFoundException;
import backend.academy.scrapper.repository.LinkRepository;
import backend.academy.scrapper.repository.TgChatLinkRepository;
import backend.academy.scrapper.repository.TgChatRepository;
import backend.academy.scrapper.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class JdbcChatService implements ChatService {

    private final TgChatRepository tgChatRepository;
    private final TgChatLinkRepository tgChatLinkRepository;
    private final LinkRepository linkRepository;

    @Override
    @Transactional
    public void registerChat(Long chatId) {
        if (tgChatRepository.isExists(chatId)) {
            throw new ChatAlreadyRegisteredException(chatId);
        }
        tgChatRepository.add(chatId);
    }

    @Override
    @Transactional
    public void deleteChat(Long chatId) {
        if (!tgChatRepository.isExists(chatId)) {
            throw new ChatNotFoundException(chatId);
        }
        var links = tgChatLinkRepository.findAllByChatId(chatId);
        tgChatLinkRepository.removeAllByChatId(chatId);
        links.forEach(link -> {
            if (tgChatLinkRepository.findAllByLinkId(link.id()).isEmpty()) {
                linkRepository.remove(link.url());
            }
        });
        tgChatRepository.remove(chatId);
    }
}
