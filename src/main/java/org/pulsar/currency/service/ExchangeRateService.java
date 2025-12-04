package org.pulsar.currency.service;

import org.pulsar.currency.dao.ExchangeRateDao;
import org.pulsar.currency.dto.currency.CurrencyResponse;
import org.pulsar.currency.dto.exchange.ExchangeRateCreateRequest;
import org.pulsar.currency.dto.exchange.ExchangeRateResponse;
import org.pulsar.currency.dto.exchange.ExchangeRequest;
import org.pulsar.currency.dto.exchange.ExchangeResponse;
import org.pulsar.currency.exception.exchange.ExchangeRateNotFoundException;
import org.pulsar.currency.model.Currency;
import org.pulsar.currency.model.ExchangeRate;
import org.pulsar.currency.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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

    public ExchangeRateResponse create(ExchangeRateCreateRequest createRequest) {
        if (isInvalid(createRequest)) {
            throw new IllegalArgumentException("Invalid create request: " + createRequest);
        }

        ExchangeRate exchangeRate = mapToExchangeRate(createRequest);
        exchangeRateDao.save(exchangeRate);

        return exchangeRateDao.findByCodes(createRequest.baseCurrencyCode(), createRequest.targetCurrencyCode())
                .map(this::mapToResponse)
                .orElseThrow();
    }

    public ExchangeRateResponse update(ExchangeRateCreateRequest updateRequest) {
        if (isInvalid(updateRequest)) {
            throw new IllegalArgumentException("Invalid update request: " + updateRequest);
        }

        ExchangeRate exchangeRate = mapToExchangeRate(updateRequest);
        exchangeRateDao.update(exchangeRate);

        return exchangeRateDao.findByCodes(updateRequest.baseCurrencyCode(), updateRequest.targetCurrencyCode())
                .map(this::mapToResponse)
                .orElseThrow();
    }

    private boolean isInvalid(ExchangeRateCreateRequest createRequest) {
        if (createRequest == null
                || StringUtils.isNullOrBlank(createRequest.baseCurrencyCode())
                || StringUtils.isNullOrBlank(createRequest.targetCurrencyCode())
                || StringUtils.isNullOrBlank(createRequest.rate())) {
            return true;
        } else if (createRequest.baseCurrencyCode().length() != 3 || createRequest.targetCurrencyCode().length() != 3) {
            return true;
        }

        try {
            BigDecimal rate = new BigDecimal(createRequest.rate());
            return rate.compareTo(BigDecimal.ZERO) <= 0;
        } catch (NumberFormatException e) {
            return true;
        }
    }

    public ExchangeResponse exchange(ExchangeRequest exchangeRequest) {
        if (isInvalid(exchangeRequest)) {
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

        throw new ExchangeRateNotFoundException();
    }

    private ExchangeResponse exchange(ExchangeRate exchangeRate, BigDecimal amount) {
        CurrencyResponse baseCurrency = mapToCurrencyResponse(exchangeRate.getBaseCurrency());
        CurrencyResponse targetCurrency = mapToCurrencyResponse(exchangeRate.getTargetCurrency());
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

    private boolean isInvalid(ExchangeRequest exchangeRequest) {
        if (exchangeRequest == null
                || StringUtils.isNullOrBlank(exchangeRequest.baseCurrencyCode())
                || StringUtils.isNullOrBlank(exchangeRequest.targetCurrencyCode())
                || StringUtils.isNullOrBlank(exchangeRequest.amount())) {
            return true;
        } else if (exchangeRequest.baseCurrencyCode().length() != 3 || exchangeRequest.targetCurrencyCode().length() != 3) {
            return true;
        }

        try {
            BigDecimal rate = new BigDecimal(exchangeRequest.amount());
            return rate.compareTo(BigDecimal.ZERO) <= 0;
        } catch (NumberFormatException e) {
            return true;
        }
    }

    private ExchangeRate mapToExchangeRate(ExchangeRateCreateRequest createRequest) {
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
