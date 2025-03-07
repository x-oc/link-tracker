package backend.academy.bot.exception;

import backend.academy.bot.dto.response.ApiErrorResponse;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
public class ApplicationExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    @NotNull
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            @NotNull MethodArgumentNotValidException ex,
            @NotNull HttpHeaders headers,
            @NotNull HttpStatusCode status,
            @NotNull WebRequest request) {
        log.atError()
                .setMessage("Method argument not valid.")
                .addKeyValue("exception", ex.getMessage())
                .addKeyValue("status", status)
                .addKeyValue("headers", headers)
                .addKeyValue("request", request)
                .log();
        return handleIncorrectRequest(ex, status);
    }

    @Override
    @NotNull
    protected ResponseEntity<Object> handleTypeMismatch(
            @NotNull TypeMismatchException ex,
            @NotNull HttpHeaders headers,
            @NotNull HttpStatusCode status,
            @NotNull WebRequest request) {
        log.atError()
                .setMessage("Type mismatch.")
                .addKeyValue("exception", ex.getMessage())
                .addKeyValue("status", status)
                .addKeyValue("headers", headers)
                .addKeyValue("request", request)
                .log();
        return handleIncorrectRequest(ex, status);
    }

    private ResponseEntity<Object> handleIncorrectRequest(Exception ex, HttpStatusCode status) {
        log.atError()
                .setMessage("Incorrect request.")
                .addKeyValue("exception", ex.getMessage())
                .addKeyValue("status", status)
                .log();
        return new ResponseEntity<>(
                new ApiErrorResponse(
                        "Incorrect request parameters.",
                        String.valueOf(status.value()),
                        ex.getClass().getSimpleName(),
                        ex.getMessage(),
                        Arrays.stream(ex.getStackTrace())
                                .map(StackTraceElement::toString)
                                .toList()),
                status);
    }
}
