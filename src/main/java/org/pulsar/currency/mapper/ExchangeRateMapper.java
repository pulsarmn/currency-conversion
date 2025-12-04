package org.pulsar.currency.mapper;

import org.pulsar.currency.dto.exchange.ExchangeRateCreateRequest;
import org.pulsar.currency.dto.exchange.ExchangeRateResponse;
import org.pulsar.currency.model.Currency;
import org.pulsar.currency.model.ExchangeRate;

import java.math.BigDecimal;
import java.util.UUID;

public class ExchangeRateMapper {

    private final CurrencyMapper currencyMapper;

    public ExchangeRateMapper(CurrencyMapper currencyMapper) {
        this.currencyMapper = currencyMapper;
    }

    public ExchangeRate map(ExchangeRateCreateRequest createRequest) {
        return ExchangeRate.builder()
                .id(UUID.randomUUID())
                .baseCurrency(Currency.builder()
                        .code(createRequest.baseCurrencyCode())
                        .build())
                .targetCurrency(Currency.builder()
                        .code(createRequest.targetCurrencyCode())
                        .build())
                .rate(new BigDecimal(createRequest.rate()))
                .build();
    }

    public ExchangeRateResponse mapToResponse(ExchangeRate exchangeRate) {
        return ExchangeRateResponse.builder()
                .id(exchangeRate.getId())
                .baseCurrency(currencyMapper.mapToResponse(exchangeRate.getBaseCurrency()))
                .targetCurrency(currencyMapper.mapToResponse(exchangeRate.getTargetCurrency()))
                .rate(exchangeRate.getRate())
                .build();
    }
}
