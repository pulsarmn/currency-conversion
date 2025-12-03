package org.pulsar.currency.dao;

import lombok.extern.slf4j.Slf4j;
import org.pulsar.currency.exception.DatabaseException;
import org.pulsar.currency.model.Currency;
import org.pulsar.currency.model.ExchangeRate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Slf4j
public class ExchangeRateDao {

    private final DataSource dataSource;

    private static final String COLUMNS = """
            er.id AS id, er.rate AS rate,
            bc.id AS bc_id, bc.code AS bc_code, bc.full_name AS bc_full_name, bc.sign AS bc_sign,
            tc.id AS tc_id, tc.code AS tc_code, tc.full_name AS tc_full_name, tc.sign AS tc_sign
            """;
    private static final String FIND_ALL = """
            SELECT %s
            FROM exchange_rates AS er
            JOIN currencies bc ON er.base_currency_id = bc.id
            JOIN currencies tc ON er.target_currency_id = tc.id
            """.formatted(COLUMNS);

    private static final String BASE_CURRENCY_PREFIX = "bc";
    private static final String TARGET_CURRENCY_PREFIX = "tc";

    public ExchangeRateDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<ExchangeRate> findAll() {
        log.info("Getting all exchange rates from the database...");

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL)) {
            ResultSet resultSet = statement.executeQuery();
            List<ExchangeRate> exchangeRates = extractList(resultSet);

            log.info("{} exchange rates has been received", exchangeRates.size());
            return exchangeRates;
        } catch (SQLException e) {
            log.error("Error when receiving exchange rates from the database", e);
            throw new DatabaseException(e);
        }
    }

    private List<ExchangeRate> extractList(ResultSet resultSet) throws SQLException {
        List<ExchangeRate> result = new ArrayList<>();
        while (resultSet.next()) {
            result.add(mapExchangeRate(resultSet));
        }
        return result;
    }

    private ExchangeRate mapExchangeRate(ResultSet resultSet) throws SQLException {
        return ExchangeRate.builder()
                .id(resultSet.getObject("id", UUID.class))
                .baseCurrency(mapCurrency(resultSet, BASE_CURRENCY_PREFIX))
                .targetCurrency(mapCurrency(resultSet, TARGET_CURRENCY_PREFIX))
                .rate(resultSet.getBigDecimal("rate"))
                .build();
    }

    private Currency mapCurrency(ResultSet resultSet, String prefix) throws SQLException {
        return Currency.builder()
                .id(resultSet.getObject(prefix + "_id", UUID.class))
                .code(resultSet.getString(prefix + "_code"))
                .fullName(resultSet.getString(prefix + "_full_name"))
                .sign(resultSet.getString(prefix + "_sign"))
                .build();
    }
}
