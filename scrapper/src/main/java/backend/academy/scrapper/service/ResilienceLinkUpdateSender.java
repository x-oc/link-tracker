package backend.academy.scrapper.service;

import backend.academy.scrapper.client.BotClient;
import backend.academy.scrapper.config.ScrapperConfig;
import backend.academy.scrapper.dto.request.LinkUpdate;
import backend.academy.scrapper.sender.HttpLinkUpdateSender;
import backend.academy.scrapper.sender.KafkaLinkUpdateSender;
import backend.academy.scrapper.sender.LinkUpdateSender;
import backend.academy.scrapper.sender.ReliableLinkUpdateSender;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@NoArgsConstructor
@Setter
public class ResilienceLinkUpdateSender implements ReliableLinkUpdateSender {

    private LinkUpdateSender updateSender;
    private LinkUpdateSender fallbackUpdateSender;

    public ResilienceLinkUpdateSender(
            Environment environment,
            KafkaTemplate<String, LinkUpdate> kafkaTemplate,
            ScrapperConfig scrapperConfig,
            BotClient botClient,
            LinkUpdateSender updateSender) {
        this.updateSender = updateSender;
        if ("HTTP".equals(environment.getProperty("app.message-transport"))) {
            fallbackUpdateSender = new KafkaLinkUpdateSender(kafkaTemplate, scrapperConfig);
        } else {
            fallbackUpdateSender = new HttpLinkUpdateSender(botClient);
        }
    }

    @Retry(name = "botRetry")
    @CircuitBreaker(name = "botCircuitBreaker", fallbackMethod = "fallback")
    @Override
    public void sendUpdateReliably(LinkUpdate linkUpdate) {
        updateSender.sendUpdate(linkUpdate);
    }

    public void fallback(LinkUpdate linkUpdate, @NotNull Exception e) {
        log.atWarn()
                .setMessage("Something went wrong, trying fallback to alternative transport")
                .addKeyValue("fallback", fallbackUpdateSender)
                .setCause(e)
                .log();
        fallbackUpdateSender.sendUpdate(linkUpdate);
    }
}
