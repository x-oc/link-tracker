package backend.academy.scrapper.api.stackoverflow;

import backend.academy.scrapper.api.EventCollectableInformationProvider;
import backend.academy.scrapper.api.LinkInformation;
import backend.academy.scrapper.api.LinkUpdateEvent;
import backend.academy.scrapper.config.ScrapperConfig;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class StackOverflowProvider extends EventCollectableInformationProvider<StackOverflowItem> {
    private static final Pattern QUESTION_PATTERN = Pattern.compile("https://stackoverflow.com/questions/(\\d+).*");
    private static final TypeReference<HashMap<String, String>> STRING_HASHMAP = new TypeReference<>() {
    };
    private final String authorizationQueryParam;
    private final ObjectMapper mapper;

    @Autowired
    public StackOverflowProvider(
        @Value("${provider.stackoverflow.url}") String apiUrl,
        ScrapperConfig config,
        ObjectMapper mapper
    ) {
        super(apiUrl);
        this.mapper = mapper;
        registerCollector(
            "AnswerEvent",
            item -> new LinkUpdateEvent(
                "stackoverflow.answers_event",
                item.lastModified(),
                Map.of("count", String.valueOf(item.answersCount()))
            )
        );
        registerCollector(
            "ScoreEvent",
            item -> new LinkUpdateEvent(
                "stackoverflow.score_event",
                item.lastModified(),
                Map.of("score", String.valueOf(item.score()))
            )
        );
        if (config.stackOverflow() != null &&
            config.stackOverflow().accessToken() != null &&
            !config.stackOverflow().accessToken().isBlank() &&
            !"${SO_ACCESS_TOKEN}".equals(config.stackOverflow().accessToken())) {
            authorizationQueryParam =
                "access_token=" + config.stackOverflow().accessToken() + "&key=" + config.stackOverflow().key();
        } else {
            authorizationQueryParam = "";
        }
    }

    @Override
    public boolean isSupported(URI url) {
        return QUESTION_PATTERN.matcher(url.toString()).matches();
    }

    @Override
    public String getType() {
        return "stackoverflow.com";
    }

    @SneakyThrows
    @Override
    public LinkInformation fetchInformation(URI url) {
        Matcher matcher = QUESTION_PATTERN.matcher(url.toString());
        if (!matcher.matches()) {
            return null;
        }
        var questionId = matcher.group(1);
        var info = executeRequest(
            "/questions/" + questionId + "?site=stackoverflow" + "&" + authorizationQueryParam,
            StackOverflowInfoResponse.class,
            StackOverflowInfoResponse.EMPTY
        );
        if (info == null || info.equals(StackOverflowInfoResponse.EMPTY) || info.items.length == 0) {
            return null;
        }
        List<LinkUpdateEvent> events = linkUpdateEventsCollector().values().stream()
            .map(stackOverflowItemLinkUpdateEventFunction ->
                stackOverflowItemLinkUpdateEventFunction.apply(info.items()[0]))
            .toList();
        HashMap<String, String> metaInformation = new HashMap<>();
        for (LinkUpdateEvent event : events) {
            metaInformation.putAll(event.additionalData());
        }
        return new LinkInformation(
            url,
            info.items()[0].title(),
            events,
            mapper.writeValueAsString(metaInformation)
        );
    }

    @SneakyThrows
    @Override
    public LinkInformation filter(LinkInformation info, OffsetDateTime after, String optionalMetaInfo) {
        Map<String, String> optionalData = new HashMap<>();
        if (optionalMetaInfo != null && !optionalMetaInfo.isEmpty()) {
            optionalData = mapper.readValue(optionalMetaInfo, STRING_HASHMAP);
        }
        List<LinkUpdateEvent> realEvents = new ArrayList<>();
        List<LinkUpdateEvent> filteredEvents =
            info.events()
                .stream()
                .filter(event -> event.lastModified().isAfter(after))
                .toList();
        for (LinkUpdateEvent event : filteredEvents) {
            for (Map.Entry<String, String> entry : event.additionalData().entrySet()) {
                if (optionalData.containsKey(entry.getKey())) {
                    String value = optionalData.get(entry.getKey());
                    if (value.equals(entry.getValue())) {
                        continue;
                    }
                }
                optionalData.put(entry.getKey(), entry.getValue());
                if (!realEvents.contains(event)) {
                    realEvents.add(event);
                }
            }
        }
        return new LinkInformation(info.url(), info.title(), realEvents, mapper.writeValueAsString(optionalData));
    }

    private record StackOverflowInfoResponse(StackOverflowItem[] items) {
        public static final StackOverflowInfoResponse EMPTY = new StackOverflowInfoResponse(null);
    }

}
