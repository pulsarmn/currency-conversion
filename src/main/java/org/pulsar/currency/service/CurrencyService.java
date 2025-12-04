package org.pulsar.currency.service;

import org.pulsar.currency.dao.CurrencyDao;
import org.pulsar.currency.dto.currency.CurrencyCreateRequest;
import org.pulsar.currency.dto.currency.CurrencyResponse;
import org.pulsar.currency.exception.currency.CurrencyAlreadyExistsException;
import org.pulsar.currency.exception.currency.CurrencyNotFoundException;
import org.pulsar.currency.mapper.CurrencyMapper;
import org.pulsar.currency.model.Currency;
import org.pulsar.currency.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

public class CurrencyService {

    private final CurrencyDao currencyDao;
    private final CurrencyMapper currencyMapper;

    public CurrencyService(CurrencyDao currencyDao, CurrencyMapper currencyMapper) {
        this.currencyDao = currencyDao;
        this.currencyMapper = currencyMapper;
    }

    public List<CurrencyResponse> getAll() {
        return currencyDao.findAll()
                .stream()
                .map(currencyMapper::mapToResponse)
                .collect(Collectors.toList());
    }

    public CurrencyResponse getByCode(String currencyCode) {
        if (StringUtils.isNullOrBlank(currencyCode)) {
            throw new IllegalArgumentException("Invalid currency code");
        }

        return currencyDao.findByCode(currencyCode)
                .map(currencyMapper::mapToResponse)
                .orElseThrow(CurrencyNotFoundException::new);
    }

    public CurrencyResponse create(CurrencyCreateRequest createRequest) {
        if (isInvalid(createRequest)) {
            throw new IllegalArgumentException("Invalid CurrencyCreateRequest");
        } else if (isExists(createRequest.code())) {
            throw new CurrencyAlreadyExistsException();
        }

        Currency currency = currencyMapper.map(createRequest);
        currencyDao.save(currency);

        return currencyMapper.mapToResponse(currency);
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
}
