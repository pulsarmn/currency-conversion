package org.pulsar.currency.dto.currency;


import lombok.Builder;

@Builder
public record CurrencyResponse(String id,
                               String code,
                               String name,
                               String sign) {
}
