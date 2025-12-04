package org.pulsar.currency.controller.currency;


import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.pulsar.currency.dto.CurrencyResponse;
import org.pulsar.currency.dto.ErrorResponse;
import org.pulsar.currency.exception.CurrencyNotFoundException;
import org.pulsar.currency.exception.DatabaseException;
import org.pulsar.currency.service.CurrencyService;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static jakarta.servlet.http.HttpServletResponse.*;

@WebServlet("/currency/*")
public class CurrencyController extends HttpServlet {

    private ObjectMapper objectMapper;
    private CurrencyService currencyService;

    private static final String CODE_PATTERN = "^/currency/(?<code>[a-zA-Z]{3})$";
    private static final Pattern PATTERN = Pattern.compile(CODE_PATTERN);

    @Override
    public void init(ServletConfig config) throws ServletException {
        ServletContext context = config.getServletContext();
        objectMapper = (ObjectMapper) context.getAttribute("objectMapper");
        currencyService = (CurrencyService) context.getAttribute("currencyService");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String jsonResponse = handleDoGet(request, response);

        response.getWriter().write(jsonResponse);
    }

    private String handleDoGet(HttpServletRequest request, HttpServletResponse response) {
        Optional<String> code = extractCode(request);
        if (code.isEmpty()) {
            response.setStatus(SC_BAD_REQUEST);
            return objectMapper.writeValueAsString(new ErrorResponse("Код валюты отсутствует или является некорректным"));
        }

        String currencyCode = code.get().toUpperCase();
        return processCode(currencyCode, response);
    }

    private String processCode(String currencyCode, HttpServletResponse response) {
        try {
            CurrencyResponse currencyResponse = currencyService.getByCode(currencyCode);
            response.setStatus(SC_OK);
            return objectMapper.writeValueAsString(currencyResponse);
        } catch (CurrencyNotFoundException | IllegalArgumentException e) {
            response.setStatus(SC_BAD_REQUEST);
            return objectMapper.writeValueAsString(new ErrorResponse("Валюта с кодом '%s' не найдена".formatted(currencyCode)));
        } catch (DatabaseException e) {
            response.setStatus(SC_INTERNAL_SERVER_ERROR);
            return objectMapper.writeValueAsString(new ErrorResponse("Ошибка базы данных"));
        } catch (Exception e) {
            response.setStatus(SC_INTERNAL_SERVER_ERROR);
            return objectMapper.writeValueAsString(new ErrorResponse("Ошибка сервера"));
        }
    }

    private Optional<String> extractCode(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        Pattern pattern = Pattern.compile(CODE_PATTERN);
        Matcher matcher = pattern.matcher(requestURI);

        return matcher.matches() ? Optional.of(matcher.group("code")) : Optional.empty();
    }
}
