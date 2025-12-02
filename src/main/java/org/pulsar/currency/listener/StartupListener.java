package org.pulsar.currency.listener;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.pulsar.currency.dao.CurrencyDao;
import org.pulsar.currency.service.CurrencyService;
import tools.jackson.databind.ObjectMapper;


@WebListener
public class StartupListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext servletContext = sce.getServletContext();

        CurrencyDao currencyDao = new CurrencyDao();
        CurrencyService currencyService = new CurrencyService(currencyDao);
        servletContext.setAttribute("currencyService", currencyService);

        ObjectMapper objectMapper = new ObjectMapper();
        servletContext.setAttribute("objectMapper", objectMapper);
    }
}
