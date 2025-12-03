package org.pulsar.currency.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;


@Data
@Builder
@AllArgsConstructor
public class ExchangeRate {

    private UUID id;
    private Currency baseCurrency;
    private Currency targetCurrency;
    private BigDecimal rate;
}
