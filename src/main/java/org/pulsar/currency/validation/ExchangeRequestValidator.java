package org.pulsar.currency.validation;

import org.pulsar.currency.dto.exchange.ExchangeRequest;
import org.pulsar.currency.util.StringUtils;

import java.math.BigDecimal;

public class ExchangeRequestValidator implements Validator<ExchangeRequest> {

    @Override
    public ValidationResult validate(ExchangeRequest exchangeRequest) {
        ValidationResult result = new ValidationResult();

        if (exchangeRequest == null) {
            result.add(new Error("Invalid exchange request"));
            return result;
        } else if (StringUtils.isNullOrBlank(exchangeRequest.baseCurrencyCode())) {
            result.add(new Error("Base currency is empty or null"));
        } else if (StringUtils.isNullOrBlank(exchangeRequest.targetCurrencyCode())) {
            result.add(new Error("Target currency is empty or null"));
        } else if (StringUtils.isNullOrBlank(exchangeRequest.amount())) {
            result.add(new Error("Amount is empty or null"));
        } else if (exchangeRequest.baseCurrencyCode().length() != 3) {
            result.add(new Error("Invalid base currency code"));
        } else if (exchangeRequest.targetCurrencyCode().length() != 3) {
            result.add(new Error("Invalid target currency code"));
        }
        validateAmount(exchangeRequest.amount(), result);

        return result;
    }

    private void validateAmount(String strAmount, ValidationResult validationResult) {
        BigDecimal amount;
        try {
            amount = new BigDecimal(strAmount);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                validationResult.add(new Error("Amount must be greater than 0"));
            }
        } catch (NumberFormatException e) {
            validationResult.add(new Error("Invalid amount value"));
        }
    }
}
