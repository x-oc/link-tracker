package backend.academy.scrapper.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "app.message-transport", havingValue = "Kafka")
public class KafkaConfig {

    @Bean
    public NewTopic newTopic(ScrapperConfig config) {
        return new NewTopic(config.kafka().updatesTopicName(), 1, (short) 1);
    }
}
