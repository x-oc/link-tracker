package backend.academy.scrapper.service;

import backend.academy.scrapper.exception.ChatAlreadyRegisteredException;
import backend.academy.scrapper.exception.ChatNotFoundException;
import backend.academy.scrapper.repository.LinkRepository;
import backend.academy.scrapper.repository.TgChatLinkRepository;
import backend.academy.scrapper.repository.TgChatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final TgChatRepository chatRepository;
    private final TgChatLinkRepository tgChatLinkRepository;
    private final LinkRepository linkRepository;

    @Transactional
    public void registerChat(Long chatId) {
        if (chatRepository.isExists(chatId)) {
            throw new ChatAlreadyRegisteredException(chatId);
        }
        chatRepository.add(chatId);
        log.atInfo()
                .setMessage("Registered new chat.")
                .addKeyValue("chatId", chatId)
                .log();
    }

    @Transactional
    public void deleteChat(Long chatId) {
        if (!chatRepository.isExists(chatId)) {
            throw new ChatNotFoundException(chatId);
        }
        var links = tgChatLinkRepository.findAllByChatId(chatId);
        tgChatLinkRepository.removeAllByChatId(chatId);
        links.forEach(link -> {
            if (tgChatLinkRepository.findAllByUrl(link.url()).isEmpty()) {
                linkRepository.remove(link.url());
            }
        });
        chatRepository.remove(chatId);
        log.atInfo().setMessage("Deleted chat.").addKeyValue("chatId", chatId).log();
    }
}
