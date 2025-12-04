package org.pulsar.currency.dao;

import lombok.extern.slf4j.Slf4j;
import org.postgresql.util.PSQLState;
import org.pulsar.currency.exception.currency.CurrencyNotFoundException;
import org.pulsar.currency.exception.DatabaseException;
import org.pulsar.currency.exception.exchange.ExchangeRateAlreadyExistsException;
import org.pulsar.currency.model.Currency;
import org.pulsar.currency.model.ExchangeRate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
    private static final String FIND_BY_CODES = FIND_ALL + " WHERE bc.code = ? AND tc.code = ?";
    private static final String SAVE = """
            INSERT INTO exchange_rates
            (id, base_currency_id, target_currency_id, rate)
            VALUES
            (?, (SELECT id FROM currencies WHERE code = ?), (SELECT id FROM currencies WHERE code = ?), ?)
            """;
    private static final String UPDATE = """
            UPDATE exchange_rates
            SET rate = ?
            WHERE base_currency_id = (SELECT id FROM currencies WHERE code = ?)
            AND target_currency_id = (SELECT id FROM currencies WHERE code = ?)
            """;

    private static final String BASE_CURRENCY_PREFIX = "bc";
    private static final String TARGET_CURRENCY_PREFIX = "tc";
    private static final String UNIQUE_CONSTRAINT = PSQLState.UNIQUE_VIOLATION.getState();
    private static final String NOT_NULL_CONSTRAINT = PSQLState.NOT_NULL_VIOLATION.getState();

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

    public Optional<ExchangeRate> findByCodes(String baseCode, String targetCode) {
        log.info("Finding exchange rate with codes ('{}', '{}')...", baseCode, targetCode);

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_CODES)) {
            statement.setString(1, baseCode);
            statement.setString(2, targetCode);

            ResultSet resultSet = statement.executeQuery();

            return extractSingle(resultSet);
        } catch (SQLException e) {
            log.error("Error while finding exchange rate with codes ('{}', '{}')", baseCode, targetCode);
            throw new DatabaseException(e);
        }
    }

    private Optional<ExchangeRate> extractSingle(ResultSet resultSet) throws SQLException {
        if (resultSet.next()) {
            log.info("Exchange rate has been found");
            return Optional.of(mapExchangeRate(resultSet));
        }
        log.info("Exchange rate hasn't been found");
        return Optional.empty();
    }

    public void save(ExchangeRate exchangeRate) {
        log.info("Saving exchange rate with codes ('{}', '{}')...",
                exchangeRate.getBaseCurrency(),
                exchangeRate.getTargetCurrency());

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(SAVE)) {
            configureStatement(statement, exchangeRate);

            statement.executeUpdate();
            log.info("Exchange rate with codes ('{}', '{}') has been successfully saved",
                    exchangeRate.getBaseCurrency(),
                    exchangeRate.getTargetCurrency());
        } catch (SQLException e) {
            handleException(e, exchangeRate);
        }
    }

    private void configureStatement(PreparedStatement statement, ExchangeRate exchangeRate) throws SQLException {
        statement.setObject(1, exchangeRate.getId());
        statement.setString(2, exchangeRate.getBaseCurrency().getCode());
        statement.setString(3, exchangeRate.getTargetCurrency().getCode());
        statement.setBigDecimal(4, exchangeRate.getRate());
    }

    public void update(ExchangeRate exchangeRate) {
        log.info("Updating exchange rate with id '{}'", exchangeRate.getId());

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE)) {
            statement.setBigDecimal(1, exchangeRate.getRate());
            statement.setString(2, exchangeRate.getBaseCurrency().getCode());
            statement.setString(3, exchangeRate.getTargetCurrency().getCode());

            statement.executeUpdate();
            log.info("Exchange rate with codes ('{}', '{}') has been successfully updated",
                    exchangeRate.getBaseCurrency(),
                    exchangeRate.getTargetCurrency());
        } catch (SQLException e) {
            handleException(e, exchangeRate);
        }
    }

    private void handleException(SQLException e, ExchangeRate exchangeRate) {
        String sqlState = e.getSQLState();
        if (sqlState.equals(UNIQUE_CONSTRAINT)) {
            log.error("A currency pair with codes ('{}', '{}') already exists",
                    exchangeRate.getBaseCurrency(),
                    exchangeRate.getTargetCurrency());
            throw new ExchangeRateAlreadyExistsException();
        } else if (sqlState.equals(NOT_NULL_CONSTRAINT)) {
            log.error("One of the currencies doesn't exist in the database ('{}', '{}')",
                    exchangeRate.getBaseCurrency(),
                    exchangeRate.getTargetCurrency());
            throw new CurrencyNotFoundException("");
        } else {
            log.error("Error while saving/updating exchange rate with codes ('{}', '{}')",
                    exchangeRate.getBaseCurrency(),
                    exchangeRate.getTargetCurrency(), e);
            throw new DatabaseException(e);
        }
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
