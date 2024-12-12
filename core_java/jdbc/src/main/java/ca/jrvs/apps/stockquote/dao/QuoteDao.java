package ca.jrvs.apps.stockquote.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Quote model.
 */
public class QuoteDao implements CrudDao<Quote, String> {

    private final Connection connection;
    
    private static final String INSERT_SQL = "INSERT INTO quote (symbol, open, high, low, price, volume, latest_trading_day, previous_close, change, change_percent, timestamp) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_SQL = "UPDATE quote SET open = ?, high = ?, low = ?, price = ?, volume = ?, latest_trading_day = ?, previous_close = ?, change = ?, change_percent = ?, timestamp = ? WHERE symbol = ?";
    private static final String SELECT_BY_ID_SQL = "SELECT * FROM quote WHERE symbol = ?";
    private static final String SELECT_ALL_SQL = "SELECT * FROM quote";
    private static final String DELETE_BY_ID_SQL = "DELETE FROM quote WHERE symbol = ?";
    private static final String DELETE_ALL_SQL = "DELETE FROM quote";

    public QuoteDao(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Quote save(Quote quote) throws IllegalArgumentException {
        if (quote == null) {
            throw new IllegalArgumentException("quote cannot be null");
        }
        if (quote.getTicker() == null) {
            throw new IllegalArgumentException("quote symbol cannot be null");
        }

        try {
            // try to update existing Quote
            try (PreparedStatement updateStatement = connection.prepareStatement(UPDATE_SQL)) {
                updateStatement.setDouble(1, quote.getOpen());
                updateStatement.setDouble(2, quote.getHigh());
                updateStatement.setDouble(3, quote.getLow());
                updateStatement.setDouble(4, quote.getPrice());
                updateStatement.setInt(5, quote.getVolume());
                updateStatement.setDate(6, quote.getLatestTradingDay());
                updateStatement.setDouble(7, quote.getPreviousClose());
                updateStatement.setDouble(8, quote.getChange());
                updateStatement.setString(9, quote.getChangePercent());
                updateStatement.setTimestamp(10, quote.getTimestamp());
                updateStatement.setString(11, quote.getTicker());

                int rowsAffected = updateStatement.executeUpdate();
                if (rowsAffected == 0) {
                    // if no rows updated, insert instead
                    try (PreparedStatement insertStatement = connection.prepareStatement(INSERT_SQL)) {
                        insertStatement.setString(1, quote.getTicker());
                        insertStatement.setDouble(2, quote.getOpen());
                        insertStatement.setDouble(3, quote.getHigh());
                        insertStatement.setDouble(4, quote.getLow());
                        insertStatement.setDouble(5, quote.getPrice());
                        insertStatement.setInt(6, quote.getVolume());
                        insertStatement.setDate(7, quote.getLatestTradingDay());
                        insertStatement.setDouble(8, quote.getPreviousClose());
                        insertStatement.setDouble(9, quote.getChange());
                        insertStatement.setString(10, quote.getChangePercent());
                        insertStatement.setTimestamp(11, quote.getTimestamp());

                        insertStatement.executeUpdate();
                    }
                }
            }

            return quote;

        } catch (SQLException e) {
            throw new RuntimeException("Error saving Quote", e);
        }
    }

    @Override
    public Optional<Quote> findById(String symbol) throws IllegalArgumentException {
        if (symbol == null) {
            throw new IllegalArgumentException("Error: symbol cannot be null");
        }

        try (PreparedStatement statement = connection.prepareStatement(SELECT_BY_ID_SQL)) {
            statement.setString(1, symbol);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    Quote quote = mapRow(rs);
                    return Optional.of(quote);
                } else {
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding Quote by symbol", e);
        }
    }

    @Override
    public Iterable<Quote> findAll() {
        List<Quote> quotes = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                Quote quote = mapRow(rs);
                quotes.add(quote);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all Quotes", e);
        }
        return quotes;
    }

    @Override
    public void deleteById(String symbol) throws IllegalArgumentException {
        if (symbol == null) {
            throw new IllegalArgumentException("Error: symbol cannot be null");
        }

        try (PreparedStatement statement = connection.prepareStatement(DELETE_BY_ID_SQL)) {
            statement.setString(1, symbol);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting Quote by symbol", e);
        }
    }

    @Override
    public void deleteAll() {
        try (PreparedStatement statement = connection.prepareStatement(DELETE_ALL_SQL)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting all Quotes", e);
        }
    }

    // helper method for findByID to map ResultSet row to quote object
    private Quote mapRow(ResultSet rs) throws SQLException {
        Quote quote = new Quote();
        quote.setTicker(rs.getString("symbol"));
        quote.setOpen(rs.getDouble("open"));
        quote.setHigh(rs.getDouble("high"));
        quote.setLow(rs.getDouble("low"));
        quote.setPrice(rs.getDouble("price"));
        quote.setVolume(rs.getInt("volume"));
        quote.setLatestTradingDay(rs.getDate("latest_trading_day"));
        quote.setPreviousClose(rs.getDouble("previous_close"));
        quote.setChange(rs.getDouble("change"));
        quote.setChangePercent(rs.getString("change_percent"));
        quote.setTimestamp(rs.getTimestamp("timestamp"));
        return quote;
    }
}
