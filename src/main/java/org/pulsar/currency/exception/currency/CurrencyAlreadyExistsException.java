package org.pulsar.currency.exception.currency;

import org.pulsar.currency.exception.ApplicationException;

public class CurrencyAlreadyExistsException extends ApplicationException {

    private final String currencyCode;

    public CurrencyAlreadyExistsException(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }
}
