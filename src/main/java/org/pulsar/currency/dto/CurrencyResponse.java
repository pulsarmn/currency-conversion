package org.pulsar.currency.dto;


import lombok.Builder;

@Builder
public record CurrencyResponse(String id,
                               String code,
                               String name,
                               String sign) {
}
