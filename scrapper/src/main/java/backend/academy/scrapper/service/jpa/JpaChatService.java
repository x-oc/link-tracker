package backend.academy.scrapper.service.jpa;

import backend.academy.scrapper.exception.ChatAlreadyRegisteredException;
import backend.academy.scrapper.exception.ChatNotFoundException;
import backend.academy.scrapper.repository.jpa.JpaChatRepository;
import backend.academy.scrapper.repository.jpa.JpaLinkRepository;
import backend.academy.scrapper.repository.jpa.entity.ChatEntity;
import backend.academy.scrapper.repository.jpa.entity.LinkEntity;
import backend.academy.scrapper.service.ChatService;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class JpaChatService implements ChatService {

    private final JpaChatRepository tgChatRepository;
    private final JpaLinkRepository linkRepository;

    @Override
    @Transactional
    public void registerChat(Long chatId) {
        if (tgChatRepository.findById(chatId).isPresent()) {
            throw new ChatAlreadyRegisteredException(chatId);
        }
        tgChatRepository.save(new ChatEntity(chatId));
    }

    @Override
    @Transactional
    public void deleteChat(Long chatId) {
        var tgChat = tgChatRepository.findById(chatId).orElseThrow(() -> new ChatNotFoundException(chatId));
        var forRemove = new ArrayList<LinkEntity>();
        for (var link : tgChat.links()) {
            tgChat.removeLink(link);
            if (link.chats().isEmpty()) {
                forRemove.add(link);
            }
        }
        linkRepository.deleteAll(forRemove);
        tgChatRepository.delete(tgChat);
    }
}
