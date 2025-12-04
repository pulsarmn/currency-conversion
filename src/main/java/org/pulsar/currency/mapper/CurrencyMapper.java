package org.pulsar.currency.mapper;

import org.pulsar.currency.dto.currency.CurrencyCreateRequest;
import org.pulsar.currency.dto.currency.CurrencyResponse;
import org.pulsar.currency.model.Currency;

import java.util.UUID;

public class CurrencyMapper {

    public Currency map(CurrencyCreateRequest createRequest) {
        return Currency.builder()
                .id(UUID.randomUUID())
                .code(createRequest.code())
                .fullName(createRequest.name())
                .sign(createRequest.sign())
                .build();
    }

    public CurrencyResponse mapToResponse(Currency currency) {
        return CurrencyResponse.builder()
                .id(currency.getId().toString())
                .code(currency.getCode())
                .name(currency.getFullName())
                .sign(currency.getSign())
                .build();
    }
}
