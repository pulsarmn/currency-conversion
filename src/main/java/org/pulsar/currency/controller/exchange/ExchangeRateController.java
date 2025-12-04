package org.pulsar.currency.controller.exchange;


import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.pulsar.currency.controller.handler.ExceptionHandler;
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
    private ExceptionHandler exceptionHandler;

    private static final String CODES_PATTERN = "^/exchangeRate/(?<basecode>[a-zA-Z]{3})(?<targetcode>[a-zA-Z]{3})$";

    @Override
    public void init(ServletConfig config) throws ServletException {
        ServletContext context = config.getServletContext();
        objectMapper = (ObjectMapper) context.getAttribute("objectMapper");
        exchangeRateService = (ExchangeRateService) context.getAttribute("exchangeRateService");
        exceptionHandler = (ExceptionHandler) context.getAttribute("exceptionHandler");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        String baseCode = extractBaseCode(requestURI);
        String targetCode = extractTargetCode(requestURI);

        try {
            ExchangeRateResponse exchangeRateResponse = exchangeRateService.getByCodes(baseCode, targetCode);
            response.setStatus(SC_OK);
            objectMapper.writeValue(response.getWriter(), exchangeRateResponse);
        } catch (Exception e) {
            exceptionHandler.handle(e, response);
        }
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (request.getMethod().equalsIgnoreCase("PATCH")) {
            doPatch(request, response);
        } else {
            super.service(request, response);
        }
    }

    @Override
    protected void doPatch(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ExchangeRateCreateRequest updateRequest = buildUpdateRequest(request);
        try {
            ExchangeRateResponse exchangeRateResponse = exchangeRateService.update(updateRequest);
            response.setStatus(SC_OK);
            objectMapper.writeValue(response.getWriter(), exchangeRateResponse);
        } catch (Exception e) {
            exceptionHandler.handle(e, response);
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

    private String extractBaseCode(String uri) {
        Matcher matcher = Pattern.compile(CODES_PATTERN).matcher(uri);
        return matcher.matches() ? matcher.group("basecode") : null;
    }

    private String extractTargetCode(String uri) {
        Matcher matcher = Pattern.compile(CODES_PATTERN).matcher(uri);
        return matcher.matches() ? matcher.group("targetcode") : null;
    }
}
