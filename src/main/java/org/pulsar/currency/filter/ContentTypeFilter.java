package org.pulsar.currency.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;

import java.io.IOException;


@WebFilter("/*")
public class ContentTypeFilter implements Filter {

    private static final String DEFAULT_CONTENT_TYPE = "application/json";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        response.setContentType(DEFAULT_CONTENT_TYPE);
        chain.doFilter(request, response);
    }
}
