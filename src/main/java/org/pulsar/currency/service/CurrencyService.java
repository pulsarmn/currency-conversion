package org.pulsar.currency.service;

import org.pulsar.currency.dao.CurrencyDao;
import org.pulsar.currency.dto.currency.CurrencyCreateRequest;
import org.pulsar.currency.dto.currency.CurrencyResponse;
import org.pulsar.currency.exception.CurrencyAlreadyExistsException;
import org.pulsar.currency.exception.CurrencyNotFoundException;
import org.pulsar.currency.model.Currency;
import org.pulsar.currency.util.StringUtils;

import java.util.List;
import java.util.UUID;
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

    public CurrencyResponse create(CurrencyCreateRequest createRequest) {
        if (isInvalid(createRequest)) {
            throw new IllegalArgumentException("Invalid CurrencyCreateRequest");
        } else if (isExists(createRequest.code())) {
            throw new CurrencyAlreadyExistsException();
        }

        Currency currency = mapToCurrency(createRequest);
        currencyDao.save(currency);

        return mapToResponse(currency);
    }

    public boolean isExists(String currencyCode) {
        return currencyDao.findByCode(currencyCode).isPresent();
    }

    private boolean isInvalid(CurrencyCreateRequest createRequest) {
        if (createRequest == null
                || StringUtils.isNullOrBlank(createRequest.code())
                || StringUtils.isNullOrBlank(createRequest.name())
                || StringUtils.isNullOrBlank(createRequest.sign())) {
            return true;
        }
        return createRequest.code().length() != 3;
    }

    private Currency mapToCurrency(CurrencyCreateRequest createRequest) {
        return Currency.builder()
                .id(UUID.randomUUID())
                .code(createRequest.code())
                .fullName(createRequest.name())
                .sign(createRequest.sign())
                .build();
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
