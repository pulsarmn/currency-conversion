package org.pulsar.currency.dto.exchange;

import lombok.Builder;
import org.pulsar.currency.dto.currency.CurrencyResponse;

import java.math.BigDecimal;
import java.util.UUID;


@Builder
public record ExchangeRateResponse(UUID id,
                                   CurrencyResponse baseCurrency,
                                   CurrencyResponse targetCurrency,
                                   BigDecimal rate) {
}
