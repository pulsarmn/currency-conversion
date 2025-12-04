package org.pulsar.currency.validation;

import org.pulsar.currency.dto.exchange.ExchangeRateCreateRequest;
import org.pulsar.currency.util.StringUtils;

import java.math.BigDecimal;

public class ExchangeCreateUpdateValidator implements Validator<ExchangeRateCreateRequest> {

    @Override
    public ValidationResult validate(ExchangeRateCreateRequest createRequest) {
        ValidationResult result = new ValidationResult();

        if (createRequest == null) {
            result.add(new Error("Create request is null"));
            return result;
        } else if (StringUtils.isNullOrBlank(createRequest.baseCurrencyCode())) {
            result.add(new Error("Base currency code is empty or null"));
        } else if (StringUtils.isNullOrBlank(createRequest.targetCurrencyCode())) {
            result.add(new Error("Target currency code is empty or null"));
        } else if (StringUtils.isNullOrBlank(createRequest.rate())) {
            result.add(new Error("Rate is empty or null"));
        } else if (createRequest.baseCurrencyCode().length() != 3) {
            result.add(new Error("Invalid base currency code"));
        } else if (createRequest.targetCurrencyCode().length() != 3) {
            result.add(new Error("Invalid target currency code"));
        }
        validateRate(createRequest.rate(), result);


        return result;
    }

    private void validateRate(String strRate, ValidationResult validationResult) {
        BigDecimal rate;
        try {
            rate = new BigDecimal(strRate);
            if (rate.compareTo(BigDecimal.ZERO) <= 0) {
                validationResult.add(new Error("Rate must be greater than 0"));
            }
        } catch (NumberFormatException e) {
            validationResult.add(new Error("Invalid rate value"));
        }
    }
}
