package org.pulsar.currency.exception.currency;

import org.pulsar.currency.exception.ApplicationException;

public class CurrencyNotFoundException extends ApplicationException {

    private final String currencyCode;

    public CurrencyNotFoundException(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }
}
