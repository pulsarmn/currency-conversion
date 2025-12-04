package org.pulsar.currency.controller.currency;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.pulsar.currency.controller.handler.ExceptionHandler;
import org.pulsar.currency.dto.currency.CurrencyCreateRequest;
import org.pulsar.currency.dto.currency.CurrencyResponse;
import org.pulsar.currency.service.CurrencyService;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

import static jakarta.servlet.http.HttpServletResponse.SC_CREATED;
import static jakarta.servlet.http.HttpServletResponse.SC_OK;


@WebServlet("/currencies")
public class CurrenciesController extends HttpServlet {

    private ObjectMapper objectMapper;
    private CurrencyService currencyService;
    private ExceptionHandler exceptionHandler;

    @Override
    public void init(ServletConfig config) throws ServletException {
        ServletContext context = config.getServletContext();
        objectMapper = (ObjectMapper) context.getAttribute("objectMapper");
        currencyService = (CurrencyService) context.getAttribute("currencyService");
        exceptionHandler = (ExceptionHandler) context.getAttribute("exceptionHandler");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            List<CurrencyResponse> currencies = currencyService.getAll();
            response.setStatus(SC_OK);
            objectMapper.writeValue(response.getWriter(), currencies);
        } catch (Exception e) {
            exceptionHandler.handle(e, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        CurrencyCreateRequest createRequest = buildCreateRequest(request);
        try {
            CurrencyResponse currencyResponse = currencyService.create(createRequest);
            response.setStatus(SC_CREATED);
            objectMapper.writeValue(response.getWriter(), currencyResponse);
        } catch (Exception e) {
            exceptionHandler.handle(e, response);
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
