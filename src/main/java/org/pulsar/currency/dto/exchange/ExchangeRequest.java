package org.pulsar.currency.dto.exchange;


import lombok.Builder;

@Builder
public record ExchangeRequest(String baseCurrencyCode,
                              String targetCurrencyCode,
                              String amount) {
}
