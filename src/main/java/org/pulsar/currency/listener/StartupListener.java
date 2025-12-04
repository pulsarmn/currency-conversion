package org.pulsar.currency.listener;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.pulsar.currency.DataSourceFactory;
import org.pulsar.currency.controller.handler.ExceptionHandler;
import org.pulsar.currency.dao.CurrencyDao;
import org.pulsar.currency.dao.ExchangeRateDao;
import org.pulsar.currency.dto.exchange.ExchangeRateCreateRequest;
import org.pulsar.currency.dto.exchange.ExchangeRequest;
import org.pulsar.currency.mapper.CurrencyMapper;
import org.pulsar.currency.mapper.ExchangeRateMapper;
import org.pulsar.currency.service.CurrencyService;
import org.pulsar.currency.service.ExchangeRateService;
import org.pulsar.currency.validation.ExchangeCreateUpdateValidator;
import org.pulsar.currency.validation.ExchangeRequestValidator;
import org.pulsar.currency.validation.Validator;
import tools.jackson.databind.ObjectMapper;

import javax.sql.DataSource;


@WebListener
public class StartupListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext servletContext = sce.getServletContext();
        DataSource dataSource = DataSourceFactory.getDataSource();

        CurrencyDao currencyDao = new CurrencyDao(dataSource);
        CurrencyMapper currencyMapper = new CurrencyMapper();
        CurrencyService currencyService = new CurrencyService(currencyDao, currencyMapper);
        servletContext.setAttribute("currencyService", currencyService);

        ExchangeRateService exchangeRateService = createExchangeRateService(dataSource, currencyMapper);
        servletContext.setAttribute("exchangeRateService", exchangeRateService);

        ObjectMapper objectMapper = new ObjectMapper();
        servletContext.setAttribute("objectMapper", objectMapper);

        ExceptionHandler exceptionHandler = new ExceptionHandler(objectMapper);
        servletContext.setAttribute("exceptionHandler", exceptionHandler);
    }

    private static ExchangeRateService createExchangeRateService(DataSource dataSource, CurrencyMapper currencyMapper) {
        ExchangeRateDao exchangeRateDao = new ExchangeRateDao(dataSource);
        ExchangeRateMapper exchangeRateMapper = new ExchangeRateMapper(currencyMapper);
        Validator<ExchangeRateCreateRequest> createRequestValidator = new ExchangeCreateUpdateValidator();
        Validator<ExchangeRequest> exchangeRequestValidator = new ExchangeRequestValidator();

        return new ExchangeRateService(exchangeRateDao,
                exchangeRateMapper,
                currencyMapper,
                createRequestValidator,
                exchangeRequestValidator);
    }
}
