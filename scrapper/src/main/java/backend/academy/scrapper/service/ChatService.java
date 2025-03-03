package backend.academy.scrapper.service;

import backend.academy.scrapper.repository.LinkRepository;
import backend.academy.scrapper.repository.TgChatLinkRepository;
import backend.academy.scrapper.repository.TgChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final TgChatRepository chatRepository;
    private final TgChatLinkRepository tgChatLinkRepository;
    private final LinkRepository linkRepository;

    @Transactional
    public void registerChat(Long chatId) {
        if (chatRepository.isExists(chatId)) {
            throw new RuntimeException(String.format("Chat %s already registered!", chatId));
        }
        chatRepository.add(chatId);
    }

    @Transactional
    public void deleteChat(Long chatId) {
        if (!chatRepository.isExists(chatId)) {
            throw new RuntimeException(String.format("Chat %s not found!", chatId));
        }
        var links = tgChatLinkRepository.findAllByChatId(chatId);
        tgChatLinkRepository.removeAllByChatId(chatId);
        links.forEach(link -> {
            if (tgChatLinkRepository.findAllByUrl(link.url()).isEmpty()) {
                linkRepository.remove(link.url());
            }
        });
        chatRepository.remove(chatId);
    }
}
