package backend.academy.bot.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic newTopic(ApplicationConfig config) {
        return new NewTopic(config.kafka().updatesTopicName() + "_dlq", 1, (short) 1);
    }
}
