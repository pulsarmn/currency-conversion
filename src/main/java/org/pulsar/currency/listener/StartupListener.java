package org.pulsar.currency.listener;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.pulsar.currency.DataSourceFactory;
import org.pulsar.currency.dao.CurrencyDao;
import org.pulsar.currency.dao.ExchangeRateDao;
import org.pulsar.currency.service.CurrencyService;
import org.pulsar.currency.service.ExchangeRateService;
import tools.jackson.databind.ObjectMapper;

import javax.sql.DataSource;


@WebListener
public class StartupListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext servletContext = sce.getServletContext();
        DataSource dataSource = DataSourceFactory.getDataSource();

        CurrencyDao currencyDao = new CurrencyDao(dataSource);
        CurrencyService currencyService = new CurrencyService(currencyDao);
        servletContext.setAttribute("currencyService", currencyService);

        ExchangeRateDao exchangeRateDao = new ExchangeRateDao(dataSource);
        ExchangeRateService exchangeRateService = new ExchangeRateService(exchangeRateDao);
        servletContext.setAttribute("exchangeRateService", exchangeRateService);

        ObjectMapper objectMapper = new ObjectMapper();
        servletContext.setAttribute("objectMapper", objectMapper);
    }
}
