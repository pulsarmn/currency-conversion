package org.pulsar.currency.dto.exchange;

import lombok.Builder;
import org.pulsar.currency.dto.currency.CurrencyResponse;

import java.math.BigDecimal;


@Builder
public record ExchangeResponse(CurrencyResponse baseCurrency,
                               CurrencyResponse targetCurrency,
                               BigDecimal rate,
                               BigDecimal amount,
                               BigDecimal convertedAmount) {
}
