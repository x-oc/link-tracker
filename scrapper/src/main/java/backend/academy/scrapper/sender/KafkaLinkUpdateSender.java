package backend.academy.scrapper.sender;

import backend.academy.scrapper.config.ScrapperConfig;
import backend.academy.scrapper.dto.request.LinkUpdate;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.message-transport", havingValue = "Kafka")
@Service
public class KafkaLinkUpdateSender implements LinkUpdateSender {

    private final KafkaTemplate<String, LinkUpdate> kafkaTemplate;
    private final ScrapperConfig config;

    @Override
    public void sendUpdate(LinkUpdate linkUpdate) {
        kafkaTemplate.send(config.kafka().updatesTopicName(), linkUpdate);
    }
}
