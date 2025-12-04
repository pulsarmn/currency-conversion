package org.pulsar.currency.exception.exchange;

import org.pulsar.currency.exception.ApplicationException;

public class ExchangeRateAlreadyExistsException extends ApplicationException {

    private final String baseCurrencyCode;
    private final String targetCurrencyCode;

    public ExchangeRateAlreadyExistsException(String baseCurrencyCode, String targetCurrencyCode) {
        this.baseCurrencyCode = baseCurrencyCode;
        this.targetCurrencyCode = targetCurrencyCode;
    }

    public String getBaseCurrencyCode() {
        return baseCurrencyCode;
    }

    public String getTargetCurrencyCode() {
        return targetCurrencyCode;
    }
}
