package org.pulsar.currency.service;

import org.pulsar.currency.dao.ExchangeRateDao;
import org.pulsar.currency.dto.CurrencyResponse;
import org.pulsar.currency.dto.ExchangeRateResponse;
import org.pulsar.currency.exception.ExchangeRateNotFoundException;
import org.pulsar.currency.model.Currency;
import org.pulsar.currency.model.ExchangeRate;
import org.pulsar.currency.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

public class ExchangeRateService {

    private final ExchangeRateDao exchangeRateDao;

    public ExchangeRateService(ExchangeRateDao exchangeRateDao) {
        this.exchangeRateDao = exchangeRateDao;
    }

    public List<ExchangeRateResponse> getAll() {
        return exchangeRateDao.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ExchangeRateResponse getByCodes(String baseCurrencyCode, String targetCurrencyCode) {
        if (StringUtils.isNullOrBlank(baseCurrencyCode) || StringUtils.isNullOrBlank(targetCurrencyCode)) {
            throw new IllegalArgumentException("Invalid currencies codes");
        } else if (baseCurrencyCode.length() != 3 || targetCurrencyCode.length() != 3) {
            throw new IllegalArgumentException("Invalid currencies codes");
        }

        return exchangeRateDao.findByCodes(baseCurrencyCode, targetCurrencyCode)
                .map(this::mapToResponse)
                .orElseThrow(ExchangeRateNotFoundException::new);
    }

    private ExchangeRateResponse mapToResponse(ExchangeRate exchangeRate) {
        return ExchangeRateResponse.builder()
                .id(exchangeRate.getId())
                .baseCurrency(mapToCurrencyResponse(exchangeRate.getBaseCurrency()))
                .targetCurrency(mapToCurrencyResponse(exchangeRate.getTargetCurrency()))
                .rate(exchangeRate.getRate())
                .build();
    }

    private CurrencyResponse mapToCurrencyResponse(Currency currency) {
        return CurrencyResponse.builder()
                .id(currency.getId().toString())
                .code(currency.getCode())
                .name(currency.getFullName())
                .sign(currency.getSign())
                .build();
    }
}
