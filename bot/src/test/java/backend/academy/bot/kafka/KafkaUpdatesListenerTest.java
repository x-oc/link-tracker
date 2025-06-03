package backend.academy.bot.kafka;

import static org.awaitility.Awaitility.await;

import backend.academy.bot.config.ApplicationConfig;
import backend.academy.bot.dto.request.LinkUpdate;
import backend.academy.bot.service.LinkNotificationService;
import com.pengrad.telegrambot.TelegramBot;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@TestPropertySource(properties = {"spring.kafka.consumer.auto-offset-reset=earliest"})
public class KafkaUpdatesListenerTest extends KafkaIntegrationEnvironment {

    @MockitoBean
    private TelegramBot telegramBot;

    @MockitoBean
    private LinkNotificationService linkNotificationService;

    @Autowired
    private KafkaTemplate<String, LinkUpdate> kafkaTemplate;

    @Autowired
    private ApplicationConfig config;

    @Autowired
    private KafkaProperties kafkaProperties;

    @Test
    @DisplayName("Тестирование KafkaUpdatesListener#listenUpdate с корректными данными")
    public void listenUpdateShouldCatchUpdate() {
        var linkUpdate = new LinkUpdate(1L, URI.create("http://test.com"), "test", List.of(1L));
        kafkaTemplate.send(config.kafka().updatesTopicName(), linkUpdate);
        await().pollInterval(Duration.ofMillis(100))
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> Mockito.verify(linkNotificationService, Mockito.times(1))
                        .notifyLinkUpdate(linkUpdate));
    }

    @Test
    @DisplayName("Тестирование KafkaUpdatesListener#listenUpdate с некорректной обработкой обновления")
    public void listenUpdateShouldProduceInDlqWhenUpdateNotConsumed() {
        var linkUpdate = new LinkUpdate(1L, URI.create("http://test.com"), "test", List.of(1L));
        Mockito.doThrow(RuntimeException.class).when(linkNotificationService).notifyLinkUpdate(linkUpdate);
        KafkaConsumer<String, LinkUpdate> dlqKafkaConsumer =
                new KafkaConsumer<>(kafkaProperties.buildConsumerProperties(null));
        dlqKafkaConsumer.subscribe(List.of(config.kafka().updatesTopicName() + "_dlq"));
        kafkaTemplate.send(config.kafka().updatesTopicName(), linkUpdate);
        await().pollInterval(Duration.ofMillis(100))
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    var values = dlqKafkaConsumer.poll(Duration.ofMillis(100));
                    Assertions.assertThat(values).hasSize(1);
                    Assertions.assertThat(values.iterator().next().value()).isEqualTo(linkUpdate);
                    Mockito.verify(linkNotificationService).notifyLinkUpdate(linkUpdate);
                });
    }
}
