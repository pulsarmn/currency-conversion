package org.pulsar.currency.dao;

import lombok.extern.slf4j.Slf4j;
import org.pulsar.currency.exception.DatabaseException;
import org.pulsar.currency.model.Currency;

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
public class CurrencyDao {

    private final DataSource dataSource;

    private static final String FIND_ALL = "SELECT id, code, full_name, sign FROM currencies";
    private static final String FIND_BY_CODE = FIND_ALL + " WHERE code = ?";
    private static final String SAVE = """
            INSERT INTO currencies (id, code, full_name, sign)
            VALUES
            (?, ?, ?, ?)
            """;

    public CurrencyDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Currency> findAll() {
        log.info("Getting currencies from a database...");

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL)) {

            ResultSet resultSet = statement.executeQuery();
            List<Currency> currencies = extractList(resultSet);

            log.info("{} currencies have been received", currencies.size());
            return currencies;
        } catch (SQLException e) {
            log.error("Error when receiving currencies", e);
            throw new DatabaseException(e);
        }
    }

    private List<Currency> extractList(ResultSet resultSet) throws SQLException {
        List<Currency> result = new ArrayList<>();
        while (resultSet.next()) {
            result.add(mapCurrency(resultSet));
        }
        return result;
    }

    public Optional<Currency> findByCode(String code) {
        log.info("Finding currency by '{}' code...", code);

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_CODE)) {
            statement.setString(1, code);
            ResultSet resultSet = statement.executeQuery();

            return extractSingle(resultSet);
        } catch (SQLException e) {
            log.error("Error when receiving currency with code '{}'", code, e);
            throw new DatabaseException(e);
        }
    }

    private Optional<Currency> extractSingle(ResultSet resultSet) throws SQLException {
        if (resultSet.next()) {
            log.info("Currency has been found");
            return Optional.of(mapCurrency(resultSet));
        }
        log.info("Currency hasn't been found");
        return Optional.empty();
    }

    public void save(Currency currency) {
        log.info("Saving currency to the database...");

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(SAVE)) {
            configureStatement(statement, currency);

            statement.executeUpdate();
            log.info("Currency with id '{}' has been successfully saved", currency.getId());
        } catch (SQLException e) {
            log.error("Error while saving currency with id '{}'", currency.getId(), e);
            throw new DatabaseException(e);
        }
    }

    private void configureStatement(PreparedStatement statement, Currency currency) throws SQLException {
        statement.setObject(1, currency.getId());
        statement.setString(2, currency.getCode());
        statement.setString(3, currency.getFullName());
        statement.setString(4, currency.getSign());
    }

    private Currency mapCurrency(ResultSet resultSet) throws SQLException {
        return Currency.builder()
                .id(resultSet.getObject("id", UUID.class))
                .code(resultSet.getString("code"))
                .fullName(resultSet.getString("full_name"))
                .sign(resultSet.getString("sign"))
                .build();
    }
}
