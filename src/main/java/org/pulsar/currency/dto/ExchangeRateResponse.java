package org.pulsar.currency.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;


@Builder
public record ExchangeRateResponse(UUID id,
                                   CurrencyResponse baseCurrency,
                                   CurrencyResponse targetCurrency,
                                   BigDecimal rate) {
}
