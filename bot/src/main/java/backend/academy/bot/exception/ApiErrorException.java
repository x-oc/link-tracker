package backend.academy.bot.exception;

import backend.academy.bot.dto.response.ApiErrorResponse;

public class ApiErrorException extends RuntimeException {

    private final ApiErrorResponse errorResponse;

    public ApiErrorException(ApiErrorResponse errorResponse) {
        super(errorResponse.exceptionMessage());
        this.errorResponse = errorResponse;
    }

    public ApiErrorResponse getErrorResponse() {
        return errorResponse;
    }
}
