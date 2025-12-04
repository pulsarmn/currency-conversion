package org.pulsar.currency.controller.handler;

import jakarta.servlet.http.HttpServletResponse;
import org.pulsar.currency.dto.ErrorResponse;
import org.pulsar.currency.exception.DatabaseException;
import org.pulsar.currency.exception.currency.CurrencyAlreadyExistsException;
import tools.jackson.databind.ObjectMapper;

import static jakarta.servlet.http.HttpServletResponse.*;

public class ExceptionHandler {

    private final ObjectMapper objectMapper;

    public ExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String handle(Exception e, HttpServletResponse response) {
        if (e instanceof CurrencyAlreadyExistsException exception) {
            response.setStatus(SC_CONFLICT);
            return createError("Валюта с кодом '%s' уже существует".formatted(exception.getCurrencyCode()));
        } else if (e instanceof DatabaseException) {
            response.setStatus(SC_INTERNAL_SERVER_ERROR);
            return createError("Ошибка базы данных");
        } else {
            response.setStatus(SC_INTERNAL_SERVER_ERROR);
            return createError("Ошибка сервера");
        }
    }

    private String createError(String message) {
        return objectMapper.writeValueAsString(new ErrorResponse(message));
    }
}
