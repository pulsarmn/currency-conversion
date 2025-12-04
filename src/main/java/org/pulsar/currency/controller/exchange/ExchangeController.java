package org.pulsar.currency.controller.exchange;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.pulsar.currency.dto.ErrorResponse;
import org.pulsar.currency.dto.exchange.ExchangeRequest;
import org.pulsar.currency.dto.exchange.ExchangeResponse;
import org.pulsar.currency.exception.DatabaseException;
import org.pulsar.currency.exception.exchange.ExchangeRateNotFoundException;
import org.pulsar.currency.service.ExchangeRateService;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

import static jakarta.servlet.http.HttpServletResponse.*;


@WebServlet("/exchange")
public class ExchangeController extends HttpServlet {

    private ObjectMapper objectMapper;
    private ExchangeRateService exchangeRateService;

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
        ExchangeRequest exchangeRequest = buildExchangeRequest(request);

        try {
            ExchangeResponse exchangeResponse = exchangeRateService.exchange(exchangeRequest);
            response.setStatus(SC_OK);
            return objectMapper.writeValueAsString(exchangeResponse);
        } catch (ExchangeRateNotFoundException e) {
            response.setStatus(SC_NOT_FOUND);
            return objectMapper.writeValueAsString(new ErrorResponse("Невозможно поменять валюты с кодами ('%s', '%s')"
                    .formatted(exchangeRequest.baseCurrencyCode(), exchangeRequest.targetCurrencyCode())));
        }
        catch (DatabaseException e) {
            response.setStatus(SC_INTERNAL_SERVER_ERROR);
            return objectMapper.writeValueAsString(new ErrorResponse("Ошибка базы данных"));
        } catch (Exception e) {
            response.setStatus(SC_INTERNAL_SERVER_ERROR);
            return objectMapper.writeValueAsString(new ErrorResponse("Ошибка сервера"));
        }
    }

    private ExchangeRequest buildExchangeRequest(HttpServletRequest request) {
        String baseCurrencyCode = request.getParameter("from");
        String targetCurrencyCode = request.getParameter("to");
        String amount = request.getParameter("amount");

        return ExchangeRequest.builder()
                .baseCurrencyCode(baseCurrencyCode)
                .targetCurrencyCode(targetCurrencyCode)
                .amount(amount)
                .build();
    }
}
