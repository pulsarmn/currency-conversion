package org.pulsar.currency.controller.exchange;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.pulsar.currency.controller.handler.ExceptionHandler;
import org.pulsar.currency.dto.exchange.ExchangeRateCreateRequest;
import org.pulsar.currency.dto.exchange.ExchangeRateResponse;
import org.pulsar.currency.service.ExchangeRateService;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

import static jakarta.servlet.http.HttpServletResponse.SC_CREATED;
import static jakarta.servlet.http.HttpServletResponse.SC_OK;


@WebServlet("/exchangeRates")
public class ExchangeRatesController extends HttpServlet {

    private ObjectMapper objectMapper;
    private ExchangeRateService exchangeRateService;
    private ExceptionHandler exceptionHandler;

    @Override
    public void init(ServletConfig config) throws ServletException {
        ServletContext context = config.getServletContext();
        objectMapper = (ObjectMapper) context.getAttribute("objectMapper");
        exchangeRateService = (ExchangeRateService) context.getAttribute("exchangeRateService");
        exceptionHandler = (ExceptionHandler) context.getAttribute("exceptionHandler");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            List<ExchangeRateResponse> exchangeRates = exchangeRateService.getAll();
            response.setStatus(SC_OK);
            objectMapper.writeValue(response.getWriter(), exchangeRates);
        } catch (Exception e) {
            exceptionHandler.handle(e, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ExchangeRateCreateRequest createRequest = buildCreateRequest(request);
        try {
            ExchangeRateResponse exchangeRateResponse = exchangeRateService.create(createRequest);
            response.setStatus(SC_CREATED);
            objectMapper.writeValue(response.getWriter(), exchangeRateResponse);
        } catch (Exception e) {
            exceptionHandler.handle(e, response);
        }
    }

    private ExchangeRateCreateRequest buildCreateRequest(HttpServletRequest request) {
        String baseCurrencyCode = request.getParameter("baseCurrencyCode");
        String targetCurrencyCode = request.getParameter("targetCurrencyCode");
        String rate = request.getParameter("rate");

        return ExchangeRateCreateRequest.builder()
                .baseCurrencyCode(baseCurrencyCode)
                .targetCurrencyCode(targetCurrencyCode)
                .rate(rate)
                .build();
    }
}
