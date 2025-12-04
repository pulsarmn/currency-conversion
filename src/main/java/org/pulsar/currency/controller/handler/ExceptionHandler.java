package org.pulsar.currency.controller.handler;

import jakarta.servlet.http.HttpServletResponse;
import org.pulsar.currency.dto.ErrorResponse;
import org.pulsar.currency.exception.DatabaseException;
import org.pulsar.currency.exception.currency.CurrencyAlreadyExistsException;
import org.pulsar.currency.exception.currency.CurrencyNotFoundException;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

import static jakarta.servlet.http.HttpServletResponse.*;

public class ExceptionHandler {

    private final ObjectMapper objectMapper;

    public ExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void handle(Exception e, HttpServletResponse response) throws IOException {
        switch (e) {
            case IllegalArgumentException exception ->
                    sendError("Отсутствует один или несколько параметров", SC_BAD_REQUEST, response);
            case CurrencyAlreadyExistsException exception -> {
                String message = "Валюта с кодом '%s' уже существует".formatted(exception.getCurrencyCode());
                sendError(message, SC_CONFLICT, response);
            }
            case CurrencyNotFoundException exception -> {
                String message = "Валюта с кодом '%s' не найдена".formatted(exception.getCurrencyCode());
                sendError(message, SC_NOT_FOUND, response);
            }
            case DatabaseException exception ->
                    sendError("Ошибка базы данных", SC_INTERNAL_SERVER_ERROR, response);
            case null, default -> sendError("Ошибка сервера", SC_INTERNAL_SERVER_ERROR, response);
        }
    }

    private void sendError(String message, int status, HttpServletResponse response) throws IOException {
        ErrorResponse error = new ErrorResponse(message);
        response.setStatus(status);
        objectMapper.writeValue(response.getWriter(), error);
    }
}
