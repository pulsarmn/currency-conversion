package org.pulsar.currency.controller.exchange;


import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.pulsar.currency.dto.ErrorResponse;
import org.pulsar.currency.dto.exchange.ExchangeRateCreateRequest;
import org.pulsar.currency.dto.exchange.ExchangeRateResponse;
import org.pulsar.currency.exception.currency.CurrencyNotFoundException;
import org.pulsar.currency.exception.DatabaseException;
import org.pulsar.currency.exception.exchange.ExchangeRateNotFoundException;
import org.pulsar.currency.service.ExchangeRateService;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
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

    @Override
    protected void doPatch(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String jsonResponse = handleDoPatch(request, response);
        response.getWriter().write(jsonResponse);
    }

    private String handleDoPatch(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ExchangeRateCreateRequest updateRequest = buildUpdateRequest(request);

        return processUpdateRequest(updateRequest, response);
    }

    private String processUpdateRequest(ExchangeRateCreateRequest updateRequest, HttpServletResponse response) {
        try {
            ExchangeRateResponse exchangeRateResponse = exchangeRateService.update(updateRequest);
            response.setStatus(SC_OK);
            return objectMapper.writeValueAsString(exchangeRateResponse);
        } catch (IllegalArgumentException e) {
            response.setStatus(SC_BAD_REQUEST);
            return objectMapper.writeValueAsString(new ErrorResponse("Отсутствует один или несколько параметров"));
        } catch (CurrencyNotFoundException e) {
            response.setStatus(SC_NOT_FOUND);
            ErrorResponse errorResponse = new ErrorResponse(
                    "Одна или обе валюты не существуют ('%s', '%s')".formatted(
                            updateRequest.baseCurrencyCode(),
                            updateRequest.targetCurrencyCode())
            );
            return objectMapper.writeValueAsString(errorResponse);
        } catch (DatabaseException e) {
            response.setStatus(SC_INTERNAL_SERVER_ERROR);
            return objectMapper.writeValueAsString(new ErrorResponse("Ошибка базы данных"));
        } catch (Exception e) {
            response.setStatus(SC_INTERNAL_SERVER_ERROR);
            return objectMapper.writeValueAsString("Ошибка сервера");
        }
    }

    private ExchangeRateCreateRequest buildUpdateRequest(HttpServletRequest request) throws IOException {
        String requestURI = request.getRequestURI();
        String baseCurrencyCode = extractBaseCode(requestURI);
        String targetCurrencyCode = extractTargetCode(requestURI);
        String rate = getParameter(request, "rate");

        return ExchangeRateCreateRequest.builder()
                .baseCurrencyCode(baseCurrencyCode)
                .targetCurrencyCode(targetCurrencyCode)
                .rate(rate)
                .build();
    }

    private String getParameter(HttpServletRequest request, String name) throws IOException {
        return getParameters(request).get(name);
    }

    private Map<String, String> getParameters(HttpServletRequest request) throws IOException {
        Map<String, String> params = new HashMap<>();
        byte[] bytes = request.getInputStream().readAllBytes();
        String body = new String(bytes);
        String[] pairs = body.split("&");

        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2) {
                params.put(keyValue[0], keyValue[1]);
            }
        }

        return params;
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (request.getMethod().equalsIgnoreCase("PATCH")) {
            doPatch(request, response);
        } else {
            super.service(request, response);
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
