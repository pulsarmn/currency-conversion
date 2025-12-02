package org.pulsar.currency.dao;

import org.pulsar.currency.model.Currency;

import java.util.ArrayList;
import java.util.List;

public class CurrencyDao {

    private final List<Currency> data = new ArrayList<>();

    public List<Currency> findAll() {
        return data;
    }
}
