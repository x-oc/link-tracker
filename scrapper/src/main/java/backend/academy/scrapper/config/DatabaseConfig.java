package backend.academy.scrapper.config;

import backend.academy.scrapper.api.InformationProvider;
import backend.academy.scrapper.repository.jdbc.JdbcChatLinkRepository;
import backend.academy.scrapper.repository.jdbc.JdbcChatRepository;
import backend.academy.scrapper.repository.jdbc.JdbcFilterRepository;
import backend.academy.scrapper.repository.jdbc.JdbcLinkRepository;
import backend.academy.scrapper.repository.jdbc.JdbcTagRepository;
import backend.academy.scrapper.repository.jpa.JpaChatRepository;
import backend.academy.scrapper.repository.jpa.JpaFilterRepository;
import backend.academy.scrapper.repository.jpa.JpaLinkRepository;
import backend.academy.scrapper.repository.jpa.JpaTagRepository;
import backend.academy.scrapper.service.ChatService;
import backend.academy.scrapper.service.LinkService;
import backend.academy.scrapper.service.jdbc.JdbcChatService;
import backend.academy.scrapper.service.jdbc.JdbcLinkService;
import backend.academy.scrapper.service.jpa.JpaChatService;
import backend.academy.scrapper.service.jpa.JpaLinkService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class DatabaseConfig {

    private final ScrapperConfig scrapperConfig;

    @Bean
    public ChatService chatService(
            JdbcChatRepository tgChatRepository,
            JdbcChatLinkRepository tgChatLinkRepository,
            JdbcLinkRepository linkRepository,
            JpaChatRepository jpaChatRepository,
            JpaLinkRepository jpaLinkRepository) {
        if (scrapperConfig.databaseAccessType().equalsIgnoreCase("jdbc")) {
            return new JdbcChatService(tgChatRepository, tgChatLinkRepository, linkRepository);
        } else if (scrapperConfig.databaseAccessType().equalsIgnoreCase("jpa")) {
            return new JpaChatService(jpaChatRepository, jpaLinkRepository);
        }
        return new JpaChatService(jpaChatRepository, jpaLinkRepository);
    }

    @Bean
    public LinkService linkService(
            JdbcLinkRepository linkRepository,
            JdbcChatLinkRepository tgChatLinkRepository,
            JdbcFilterRepository jdbcFilterRepository,
            JdbcTagRepository jdbcTagRepository,
            Map<String, InformationProvider> informationProviders,
            JpaLinkRepository jpaLinkRepository,
            JpaChatRepository jpaChatRepository,
            JpaFilterRepository jpaFilterRepository,
            JpaTagRepository jpaTagRepository) {
        if (scrapperConfig.databaseAccessType().equalsIgnoreCase("jdbc")) {
            return new JdbcLinkService(
                    linkRepository,
                    tgChatLinkRepository,
                    jdbcTagRepository,
                    jdbcFilterRepository,
                    informationProviders);
        } else if (scrapperConfig.databaseAccessType().equalsIgnoreCase("jpa")) {
            return new JpaLinkService(
                    jpaLinkRepository, jpaChatRepository, jpaTagRepository, jpaFilterRepository, informationProviders);
        }
        return new JpaLinkService(
                jpaLinkRepository, jpaChatRepository, jpaTagRepository, jpaFilterRepository, informationProviders);
    }
}
