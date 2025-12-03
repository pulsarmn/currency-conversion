package org.pulsar.currency.service;

import org.pulsar.currency.dao.CurrencyDao;
import org.pulsar.currency.dto.CurrencyResponse;
import org.pulsar.currency.exception.CurrencyNotFoundException;
import org.pulsar.currency.model.Currency;
import org.pulsar.currency.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

public class CurrencyService {

    private final CurrencyDao currencyDao;

    public CurrencyService(CurrencyDao currencyDao) {
        this.currencyDao = currencyDao;
    }

    public List<CurrencyResponse> getAll() {
        return currencyDao.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public CurrencyResponse getByCode(String currencyCode) {
        if (StringUtils.isNullOrBlank(currencyCode)) {
            throw new IllegalArgumentException("Invalid currency code");
        }

        return currencyDao.findByCode(currencyCode)
                .map(this::mapToResponse)
                .orElseThrow(CurrencyNotFoundException::new);
    }

    private CurrencyResponse mapToResponse(Currency currency) {
        return CurrencyResponse.builder()
                .id(currency.getId().toString())
                .code(currency.getCode())
                .name(currency.getFullName())
                .sign(currency.getSign())
                .build();
    }
}
