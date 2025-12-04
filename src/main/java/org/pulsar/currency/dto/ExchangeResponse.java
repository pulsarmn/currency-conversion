package org.pulsar.currency.dto;

import lombok.Builder;

import java.math.BigDecimal;


@Builder
public record ExchangeResponse(CurrencyResponse baseCurrency,
                               CurrencyResponse targetCurrency,
                               BigDecimal rate,
                               BigDecimal amount,
                               BigDecimal convertedAmount) {
}
