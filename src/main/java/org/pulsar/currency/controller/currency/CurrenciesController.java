package org.pulsar.currency.controller.currency;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.pulsar.currency.dto.currency.CurrencyCreateRequest;
import org.pulsar.currency.dto.currency.CurrencyResponse;
import org.pulsar.currency.dto.ErrorResponse;
import org.pulsar.currency.exception.currency.CurrencyAlreadyExistsException;
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

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String jsonResponse = handleDoPost(request, response);
        response.getWriter().write(jsonResponse);
    }

    private String handleDoPost(HttpServletRequest request, HttpServletResponse response) {
        CurrencyCreateRequest createRequest = buildCreateRequest(request);

        return processCreateRequest(createRequest, response);
    }

    private String processCreateRequest(CurrencyCreateRequest createRequest, HttpServletResponse response) {
        try {
            CurrencyResponse currencyResponse = currencyService.create(createRequest);
            response.setStatus(SC_CREATED);
            return objectMapper.writeValueAsString(currencyResponse);
        } catch (IllegalArgumentException e) {
            response.setStatus(SC_BAD_REQUEST);
            return objectMapper.writeValueAsString(new ErrorResponse("Отсутствует один или несколько параметров"));
        } catch (CurrencyAlreadyExistsException e) {
            response.setStatus(SC_CONFLICT);
            return objectMapper.writeValueAsString(new ErrorResponse("Валюта с кодом '%s' уже существует".formatted(createRequest.code())));
        } catch (DatabaseException e) {
            response.setStatus(SC_INTERNAL_SERVER_ERROR);
            return objectMapper.writeValueAsString(new ErrorResponse("Ошибка базы данных"));
        } catch (Exception e) {
            response.setStatus(SC_INTERNAL_SERVER_ERROR);
            return objectMapper.writeValueAsString(new ErrorResponse("Ошибка сервера"));
        }
    }

    private CurrencyCreateRequest buildCreateRequest(HttpServletRequest request) {
        String code = request.getParameter("code");
        String name = request.getParameter("name");
        String sign = request.getParameter("sign");

        return CurrencyCreateRequest.builder()
                .code(code)
                .name(name)
                .sign(sign)
                .build();
    }
}
