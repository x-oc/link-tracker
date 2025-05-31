package backend.academy.scrapper.api.stackoverflow;

import backend.academy.scrapper.api.LinkInformation;
import backend.academy.scrapper.api.LinkUpdateEvent;
import backend.academy.scrapper.api.WebClientInformationProvider;
import backend.academy.scrapper.config.ScrapperConfig;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StackOverflowProvider extends WebClientInformationProvider {
    private static final Pattern QUESTION_PATTERN = Pattern.compile("https://stackoverflow.com/questions/(\\d+).*");
    private final String authorizationQueryParam;

    @Autowired
    public StackOverflowProvider(@Value("${provider.stackoverflow.url}") String apiUrl, ScrapperConfig config) {
        super(apiUrl);
        if (config.stackOverflow() != null
                && config.stackOverflow().accessToken() != null
                && !config.stackOverflow().accessToken().isBlank()
                && !"${SO_ACCESS_TOKEN}".equals(config.stackOverflow().accessToken())) {
            authorizationQueryParam = "access_token=%s&key=%s"
                    .formatted(
                            config.stackOverflow().accessToken(),
                            config.stackOverflow().key());
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
            log.atWarn()
                    .setMessage("Trying to fetch unsupported url.")
                    .addKeyValue("url", url)
                    .addKeyValue("provider", "stackoverflow")
                    .log();
            return null;
        }
        var questionId = matcher.group(1);

        var questionUri = "/questions/%s?site=stackoverflow&%s".formatted(questionId, authorizationQueryParam);
        SoApiQuestionsResponse questionInfo =
                executeRequest(questionUri, SoApiQuestionsResponse.class, SoApiQuestionsResponse.EMPTY);
        var answersUri = "/questions/%s/answers?site=stackoverflow&filter=withbody&%s"
                .formatted(questionId, authorizationQueryParam);
        SoApiAnswersResponse answersInfo =
                executeRequest(answersUri, SoApiAnswersResponse.class, SoApiAnswersResponse.EMPTY);

        if (SoNoInfoReturned(questionInfo, answersInfo, url)) {
            return null;
        }

        String questionTitle = questionInfo.items()[0].title();

        List<LinkUpdateEvent> events = Arrays.stream(answersInfo.items())
                .map(answer -> {
                    String body = answer.body();
                    if (body == null) {
                        body = "No answer";
                    }
                    body = body.length() > 200 ? "%s ...".formatted(body.substring(0, 200)) : body;
                    return new LinkUpdateEvent(
                            "There is new answer by user %s on the question '%s': %s"
                                    .formatted(answer.owner().name(), questionTitle, body),
                            answer.creationDate());
                })
                .toList();
        return new LinkInformation(url, questionInfo.items()[0].title(), events);
    }

    @SneakyThrows
    @Override
    public LinkInformation filter(LinkInformation info, OffsetDateTime after) {
        List<LinkUpdateEvent> filteredEvents = info.events().stream()
                .filter(event -> event.lastUpdated().isAfter(after))
                .toList();
        return new LinkInformation(info.url(), info.title(), filteredEvents);
    }

    private boolean SoNoInfoReturned(SoApiQuestionsResponse questionInfo, SoApiAnswersResponse answersInfo, URI url) {
        boolean SoNoInfoReturned = questionInfo == null
                || answersInfo == null
                || questionInfo.equals(SoApiQuestionsResponse.EMPTY)
                || answersInfo.equals(SoApiAnswersResponse.EMPTY)
                || questionInfo.items.length == 0
                || answersInfo.items.length == 0;

        if (SoNoInfoReturned) {
            log.atWarn()
                    .setMessage("StackOverflow returned no info.")
                    .addKeyValue("url", url)
                    .log();
        }
        return SoNoInfoReturned;
    }

    private record SoApiQuestionsResponse(StackOverflowQuestionDTO[] items) {
        public static final SoApiQuestionsResponse EMPTY = new SoApiQuestionsResponse(null);
    }

    private record SoApiAnswersResponse(StackOverflowAnswersDTO[] items) {
        public static final SoApiAnswersResponse EMPTY = new SoApiAnswersResponse(null);
    }
}
