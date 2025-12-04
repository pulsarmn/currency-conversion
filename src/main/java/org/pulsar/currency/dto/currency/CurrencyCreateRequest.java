package org.pulsar.currency.dto.currency;


import lombok.Builder;

@Builder
public record CurrencyCreateRequest(String code,
                                    String name,
                                    String sign) {
}
