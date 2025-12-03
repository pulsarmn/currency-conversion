package org.pulsar.currency.dto;


import lombok.Builder;

@Builder
public record CurrencyCreateRequest(String code,
                                    String name,
                                    String sign) {
}
