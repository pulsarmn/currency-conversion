package org.pulsar.currency.controller;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.pulsar.currency.dto.CurrencyResponse;
import org.pulsar.currency.dto.ErrorResponse;
import org.pulsar.currency.exception.DatabaseException;
import org.pulsar.currency.service.CurrencyService;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

import static jakarta.servlet.http.HttpServletResponse.*;


@WebServlet("/currencies")
public class CurrenciesController extends HttpServlet {

    private ObjectMapper objectMapper;
    private CurrencyService currencyService;

    @Override
    public void init(ServletConfig config) throws ServletException {
        ServletContext servletContext = config.getServletContext();
        objectMapper = (ObjectMapper) servletContext.getAttribute("objectMapper");
        currencyService = (CurrencyService) servletContext.getAttribute("currencyService");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String jsonResponse = handleDoGet(response);
        response.getWriter().write(jsonResponse);
    }

    private String handleDoGet(HttpServletResponse response) {
        try {
            List<CurrencyResponse> currencies = currencyService.getAll();
            response.setStatus(SC_OK);
            return objectMapper.writeValueAsString(currencies);
        } catch (DatabaseException e) {
            response.setStatus(SC_INTERNAL_SERVER_ERROR);
            return objectMapper.writeValueAsString(new ErrorResponse("Ошибка базы данных"));
        } catch (Exception e) {
            response.setStatus(SC_INTERNAL_SERVER_ERROR);
            return objectMapper.writeValueAsString(new ErrorResponse("Ошибка сервера. Уже работаем над её исправлением"));
        }
    }
}
