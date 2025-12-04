package org.pulsar.currency.dto.exchange;


import lombok.Builder;

@Builder
public record ExchangeRateCreateRequest(String baseCurrencyCode,
                                        String targetCurrencyCode,
                                        String rate) {
}
