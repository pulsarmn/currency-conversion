package org.pulsar.currency.controller;


import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.pulsar.currency.dto.ErrorResponse;
import org.pulsar.currency.dto.ExchangeRateResponse;
import org.pulsar.currency.exception.DatabaseException;
import org.pulsar.currency.exception.ExchangeRateNotFoundException;
import org.pulsar.currency.service.ExchangeRateService;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static jakarta.servlet.http.HttpServletResponse.*;

@WebServlet("/exchangeRate/*")
public class ExchangeRateController extends HttpServlet {

    private ObjectMapper objectMapper;
    private ExchangeRateService exchangeRateService;

    private static final String CODES_PATTERN = "^/exchangeRate/(?<basecode>[a-zA-Z]{3})(?<targetcode>[a-zA-Z]{3})$";

    @Override
    public void init(ServletConfig config) throws ServletException {
        ServletContext context = config.getServletContext();
        objectMapper = (ObjectMapper) context.getAttribute("objectMapper");
        exchangeRateService = (ExchangeRateService) context.getAttribute("exchangeRateService");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String jsonResponse = handleDoGet(request, response);
        response.getWriter().write(jsonResponse);
    }

    private String handleDoGet(HttpServletRequest request, HttpServletResponse response) {
        String requestURI = request.getRequestURI();
        String baseCode = extractBaseCode(requestURI);
        String targetCode = extractTargetCode(requestURI);

        try {
            ExchangeRateResponse exchangeRateResponse = exchangeRateService.getByCodes(baseCode, targetCode);
            response.setStatus(SC_OK);
            return objectMapper.writeValueAsString(exchangeRateResponse);
        } catch (IllegalArgumentException e) {
            response.setStatus(SC_BAD_REQUEST);
            return objectMapper.writeValueAsString(new ErrorResponse("Код валюты отсутствует в адресе: " + requestURI));
        } catch (ExchangeRateNotFoundException e) {
            response.setStatus(SC_NOT_FOUND);
            return objectMapper.writeValueAsString(
                    new ErrorResponse("Валютная пара с кодами ('%s', '%s') не найдена".formatted(baseCode, targetCode)));
        } catch (DatabaseException e) {
            response.setStatus(SC_INTERNAL_SERVER_ERROR);
            return objectMapper.writeValueAsString(new ErrorResponse("Ошибка базы данных"));
        } catch (Exception e) {
            response.setStatus(SC_INTERNAL_SERVER_ERROR);
            return objectMapper.writeValueAsString(new ErrorResponse("Ошибка сервера"));
        }
    }

    private String extractBaseCode(String uri) {
        Matcher matcher = Pattern.compile(CODES_PATTERN).matcher(uri);
        return matcher.matches() ? matcher.group("basecode") : null;
    }

    private String extractTargetCode(String uri) {
        Matcher matcher = Pattern.compile(CODES_PATTERN).matcher(uri);
        return matcher.matches() ? matcher.group("targetcode") : null;
    }
}
