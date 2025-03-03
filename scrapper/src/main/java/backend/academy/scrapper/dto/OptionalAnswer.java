package backend.academy.scrapper.dto;

import backend.academy.scrapper.dto.response.ApiErrorResponse;
import backend.academy.scrapper.util.OptionalAnswerDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.function.Consumer;

@JsonDeserialize(using = OptionalAnswerDeserializer.class)
public record OptionalAnswer<T>(T answer, ApiErrorResponse apiErrorResponse) {

    public static <T> OptionalAnswer<T> error(ApiErrorResponse apiErrorResponse) {
        return new OptionalAnswer<>(null, apiErrorResponse);
    }

    public static <T> OptionalAnswer<T> of(T t) {
        return new OptionalAnswer<>(t, null);
    }

    public boolean isError() {
        return apiErrorResponse != null;
    }

    public OptionalAnswer<T> ifExists(Consumer<T> consumer) {
        if (answer != null) {
            consumer.accept(answer);
        }
        return this;
    }
}
