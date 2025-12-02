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
import java.util.UUID;


@Slf4j
public class CurrencyDao {

    private final DataSource dataSource;

    private static final String FIND_ALL = "SELECT id, code, full_name, sign FROM currencies";

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

    private Currency mapCurrency(ResultSet resultSet) throws SQLException {
        return Currency.builder()
                .id(resultSet.getObject("id", UUID.class))
                .code(resultSet.getString("code"))
                .fullName(resultSet.getString("full_name"))
                .sign(resultSet.getString("sign"))
                .build();
    }
}
