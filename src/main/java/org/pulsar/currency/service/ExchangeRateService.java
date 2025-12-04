package org.pulsar.currency.service;

import org.pulsar.currency.dao.ExchangeRateDao;
import org.pulsar.currency.dto.currency.CurrencyResponse;
import org.pulsar.currency.dto.exchange.ExchangeRateCreateRequest;
import org.pulsar.currency.dto.exchange.ExchangeRateResponse;
import org.pulsar.currency.dto.exchange.ExchangeRequest;
import org.pulsar.currency.dto.exchange.ExchangeResponse;
import org.pulsar.currency.exception.exchange.ExchangeRateNotFoundException;
import org.pulsar.currency.mapper.CurrencyMapper;
import org.pulsar.currency.mapper.ExchangeRateMapper;
import org.pulsar.currency.model.Currency;
import org.pulsar.currency.model.ExchangeRate;
import org.pulsar.currency.util.StringUtils;
import org.pulsar.currency.validation.Validator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// Можно и даже нужно продолжить рефакторинг, но мне лень :)
public class ExchangeRateService {

    private final ExchangeRateDao exchangeRateDao;
    private final ExchangeRateMapper exchangeRateMapper;
    private final CurrencyMapper currencyMapper;
    private final Validator<ExchangeRateCreateRequest> createRequestValidator;
    private final Validator<ExchangeRequest> exchangeRequestValidator;

    public ExchangeRateService(ExchangeRateDao exchangeRateDao, ExchangeRateMapper exchangeRateMapper, CurrencyMapper currencyMapper, Validator<ExchangeRateCreateRequest> createRequestValidator, Validator<ExchangeRequest> exchangeRequestValidator) {
        this.exchangeRateDao = exchangeRateDao;
        this.exchangeRateMapper = exchangeRateMapper;
        this.currencyMapper = currencyMapper;
        this.createRequestValidator = createRequestValidator;
        this.exchangeRequestValidator = exchangeRequestValidator;
    }

    public List<ExchangeRateResponse> getAll() {
        return exchangeRateDao.findAll()
                .stream()
                .map(exchangeRateMapper::mapToResponse)
                .collect(Collectors.toList());
    }

    public ExchangeRateResponse getByCodes(String baseCurrencyCode, String targetCurrencyCode) {
        if (StringUtils.isNullOrBlank(baseCurrencyCode) || StringUtils.isNullOrBlank(targetCurrencyCode)) {
            throw new IllegalArgumentException("Invalid currencies codes");
        } else if (baseCurrencyCode.length() != 3 || targetCurrencyCode.length() != 3) {
            throw new IllegalArgumentException("Invalid currencies codes");
        }

        return exchangeRateDao.findByCodes(baseCurrencyCode, targetCurrencyCode)
                .map(exchangeRateMapper::mapToResponse)
                .orElseThrow(() -> new ExchangeRateNotFoundException(baseCurrencyCode, targetCurrencyCode));
    }

    public ExchangeRateResponse create(ExchangeRateCreateRequest createRequest) {
        if (!createRequestValidator.validate(createRequest).isValid()) {
            throw new IllegalArgumentException("Invalid create request: " + createRequest);
        }

        ExchangeRate exchangeRate = exchangeRateMapper.map(createRequest);
        exchangeRateDao.save(exchangeRate);

        return exchangeRateDao.findByCodes(createRequest.baseCurrencyCode(), createRequest.targetCurrencyCode())
                .map(exchangeRateMapper::mapToResponse)
                .orElseThrow();
    }

    public ExchangeRateResponse update(ExchangeRateCreateRequest updateRequest) {
        if (!createRequestValidator.validate(updateRequest).isValid()) {
            throw new IllegalArgumentException("Invalid update request: " + updateRequest);
        }

        ExchangeRate exchangeRate = exchangeRateMapper.map(updateRequest);
        exchangeRateDao.update(exchangeRate);

        return exchangeRateDao.findByCodes(updateRequest.baseCurrencyCode(), updateRequest.targetCurrencyCode())
                .map(exchangeRateMapper::mapToResponse)
                .orElseThrow();
    }

    public ExchangeResponse exchange(ExchangeRequest exchangeRequest) {
        if (!exchangeRequestValidator.validate(exchangeRequest).isValid()) {
            throw new IllegalArgumentException();
        }

        Optional<ExchangeRate> directExchangeRate = exchangeRateDao.findByCodes(
                exchangeRequest.baseCurrencyCode(),
                exchangeRequest.targetCurrencyCode()
        );

        BigDecimal amount = new BigDecimal(exchangeRequest.amount());
        if (directExchangeRate.isPresent()) {
            return exchange(directExchangeRate.get(), amount);
        }

        Optional<ExchangeRate> reverseExchangeRate = exchangeRateDao.findByCodes(
                exchangeRequest.targetCurrencyCode(),
                exchangeRequest.baseCurrencyCode());

        if (reverseExchangeRate.isPresent()) {
            ExchangeRate exchangeRate = reverseExchangeRate.get();
            Currency baseCurrency = exchangeRate.getBaseCurrency();
            Currency targetCurrency = exchangeRate.getTargetCurrency();
            exchangeRate.setBaseCurrency(targetCurrency);
            exchangeRate.setTargetCurrency(baseCurrency);
            exchangeRate.setRate(BigDecimal.ONE.divide(exchangeRate.getRate(), 6, RoundingMode.HALF_UP));
            return exchange(exchangeRate, amount);
        }

        String usdCode = "USD";
        Optional<ExchangeRate> baseExchangeRate = exchangeRateDao.findByCodes(usdCode, exchangeRequest.baseCurrencyCode());
        Optional<ExchangeRate> targetExchangeRate = exchangeRateDao.findByCodes(usdCode, exchangeRequest.targetCurrencyCode());

        if (baseExchangeRate.isPresent() && targetExchangeRate.isPresent()) {
            BigDecimal rate = baseExchangeRate.get().getRate().divide(targetExchangeRate.get().getRate(), 6, RoundingMode.HALF_UP);
            ExchangeRate build = ExchangeRate.builder()
                    .baseCurrency(baseExchangeRate.get().getTargetCurrency())
                    .targetCurrency(targetExchangeRate.get().getTargetCurrency())
                    .rate(rate)
                    .build();
            return exchange(build, amount);
        }

        throw new ExchangeRateNotFoundException(exchangeRequest.baseCurrencyCode(), exchangeRequest.targetCurrencyCode());
    }

    private ExchangeResponse exchange(ExchangeRate exchangeRate, BigDecimal amount) {
        CurrencyResponse baseCurrency = currencyMapper.mapToResponse(exchangeRate.getBaseCurrency());
        CurrencyResponse targetCurrency = currencyMapper.mapToResponse(exchangeRate.getTargetCurrency());
        BigDecimal rate = exchangeRate.getRate();
        BigDecimal convertedAmount = rate.multiply(amount);

        return ExchangeResponse.builder()
                .baseCurrency(baseCurrency)
                .targetCurrency(targetCurrency)
                .rate(exchangeRate.getRate())
                .amount(amount)
                .convertedAmount(convertedAmount)
                .build();
    }
}
