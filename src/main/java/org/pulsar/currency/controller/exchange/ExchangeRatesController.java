package org.pulsar.currency.controller.exchange;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.pulsar.currency.dto.ErrorResponse;
import org.pulsar.currency.dto.ExchangeRateCreateRequest;
import org.pulsar.currency.dto.ExchangeRateResponse;
import org.pulsar.currency.exception.CurrencyNotFoundException;
import org.pulsar.currency.exception.DatabaseException;
import org.pulsar.currency.exception.ExchangeRateAlreadyExistsException;
import org.pulsar.currency.service.ExchangeRateService;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

import static jakarta.servlet.http.HttpServletResponse.*;


@WebServlet("/exchangeRates")
public class ExchangeRatesController extends HttpServlet {

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
        String jsonResponse = handleDoGet(response);
        response.getWriter().write(jsonResponse);
    }

    private String handleDoGet(HttpServletResponse response) {
        try {
            List<ExchangeRateResponse> exchangeRates = exchangeRateService.getAll();
            response.setStatus(SC_OK);
            return objectMapper.writeValueAsString(exchangeRates);
        } catch (DatabaseException e) {
            response.setStatus(SC_INTERNAL_SERVER_ERROR);
            return objectMapper.writeValueAsString(new ErrorResponse("Ошибка базы данных"));
        } catch (Exception e) {
            response.setStatus(SC_INTERNAL_SERVER_ERROR);
            return objectMapper.writeValueAsString(new ErrorResponse("Ошибка сервера"));
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String jsonResponse = handleDoPost(request, response);
        response.getWriter().write(jsonResponse);
    }

    private String handleDoPost(HttpServletRequest request, HttpServletResponse response) {
        ExchangeRateCreateRequest createRequest = buildCreateRequest(request);

        return processCreateRequest(createRequest, response);
    }

    private String processCreateRequest(ExchangeRateCreateRequest createRequest, HttpServletResponse response) {
        try {
            ExchangeRateResponse exchangeRateResponse = exchangeRateService.create(createRequest);
            response.setStatus(SC_CREATED);
            return objectMapper.writeValueAsString(exchangeRateResponse);
        } catch (IllegalArgumentException e) {
            response.setStatus(SC_BAD_REQUEST);
            return objectMapper.writeValueAsString(new ErrorResponse("Отсутствует один или несколько параметров"));
        } catch (CurrencyNotFoundException e) {
            response.setStatus(SC_NOT_FOUND);
            ErrorResponse errorResponse = new ErrorResponse(
                    "Одна или обе валюты не существуют ('%s', '%s')".formatted(
                            createRequest.baseCurrencyCode(),
                            createRequest.targetCurrencyCode())
            );
            return objectMapper.writeValueAsString(errorResponse);
        } catch (ExchangeRateAlreadyExistsException e) {
            response.setStatus(SC_CONFLICT);
            ErrorResponse errorResponse = new ErrorResponse(
                    "Валютная пара с кодами ('%s', '%s') уже существует".formatted(
                            createRequest.baseCurrencyCode(),
                            createRequest.targetCurrencyCode())
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
