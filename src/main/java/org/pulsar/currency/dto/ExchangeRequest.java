package org.pulsar.currency.dto;


import lombok.Builder;

@Builder
public record ExchangeRequest(String baseCurrencyCode,
                              String targetCurrencyCode,
                              String amount) {
}
