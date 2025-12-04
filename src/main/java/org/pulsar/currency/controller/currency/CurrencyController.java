package org.pulsar.currency.controller.currency;


import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.pulsar.currency.controller.handler.ExceptionHandler;
import org.pulsar.currency.dto.currency.CurrencyResponse;
import org.pulsar.currency.dto.ErrorResponse;
import org.pulsar.currency.exception.currency.CurrencyNotFoundException;
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
    private ExceptionHandler exceptionHandler;

    private static final String CODE_PATTERN = "^/currency/(?<code>[a-zA-Z]{3})$";

    @Override
    public void init(ServletConfig config) throws ServletException {
        ServletContext context = config.getServletContext();
        objectMapper = (ObjectMapper) context.getAttribute("objectMapper");
        currencyService = (CurrencyService) context.getAttribute("currencyService");
        exceptionHandler = (ExceptionHandler) context.getAttribute("exceptionHandler");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String currencyCode = extractCode(request);
        if (currencyCode == null) {
            response.setStatus(SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(), new ErrorResponse("Код валюты отсутствует или является некорректным"));
        } else {
            try {
                CurrencyResponse currencyResponse = currencyService.getByCode(currencyCode);
                response.setStatus(SC_OK);
                objectMapper.writeValue(response.getWriter(), currencyResponse);
            } catch (Exception e) {
                exceptionHandler.handle(e, response);
            }
        }
    }

    private String extractCode(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        Pattern pattern = Pattern.compile(CODE_PATTERN);
        Matcher matcher = pattern.matcher(requestURI);

        return matcher.matches() ? matcher.group("code") : null;
    }
}
