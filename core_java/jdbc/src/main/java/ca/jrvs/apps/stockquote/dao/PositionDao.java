package ca.jrvs.apps.stockquote.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Position model.
 */
public class PositionDao implements CrudDao<Position, String> {

    private final Connection connection;

    private static final String INSERT_SQL = "INSERT INTO position (symbol, number_of_shares, value_paid) VALUES (?, ?, ?)";
    private static final String UPDATE_SQL = "UPDATE position SET number_of_shares = ?, value_paid = ? WHERE symbol = ?";
    private static final String SELECT_BY_ID_SQL = "SELECT * FROM position WHERE symbol = ?";
    private static final String SELECT_ALL_SQL = "SELECT * FROM position";
    private static final String DELETE_BY_ID_SQL = "DELETE FROM position WHERE symbol = ?";
    private static final String DELETE_ALL_SQL = "DELETE FROM position";

    public PositionDao(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Position save(Position position) throws IllegalArgumentException {
        if (position == null) {
            throw new IllegalArgumentException("position cannot be null");
        }
        if (position.getTicker() == null) {
            throw new IllegalArgumentException("Position symbol cannot be null");
        }

        try {
            // try to update existing Position
            try (PreparedStatement updateStmt = connection.prepareStatement(UPDATE_SQL)) {
                updateStmt.setInt(1, position.getNumOfShares());
                updateStmt.setDouble(2, position.getValuePaid());
                updateStmt.setString(3, position.getTicker());

                int rowsAffected = updateStmt.executeUpdate();
                if (rowsAffected == 0) {
                    // if no rows updated, insert instead
                    try (PreparedStatement insertStmt = connection.prepareStatement(INSERT_SQL)) {
                        insertStmt.setString(1, position.getTicker());
                        insertStmt.setInt(2, position.getNumOfShares());
                        insertStmt.setDouble(3, position.getValuePaid());

                        insertStmt.executeUpdate();
                    }
                }
            }

            return position;

        } catch (SQLException e) {
            throw new RuntimeException("Error saving Position", e);
        }
    }

    @Override
    public Optional<Position> findById(String symbol) throws IllegalArgumentException {
        if (symbol == null) {
            throw new IllegalArgumentException("Error: symbol cannot be null");
        }

        try (PreparedStatement stmt = connection.prepareStatement(SELECT_BY_ID_SQL)) {
            stmt.setString(1, symbol);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Position position = mapRow(rs);
                    return Optional.of(position);
                } else {
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding Position by symbol", e);
        }
    }

    /**
     * Retrieves all Positions from the database.
     *
     * @return Iterable of all Positions
     */
    @Override
    public Iterable<Position> findAll() {
        List<Position> positions = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Position position = mapRow(rs);
                positions.add(position);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all Positions", e);
        }
        return positions;
    }

    /**
     * Deletes a Position by its symbol.
     *
     * @param symbol Symbol of the Position to delete
     * @throws IllegalArgumentException if the symbol is null
     */
    @Override
    public void deleteById(String symbol) throws IllegalArgumentException {
        if (symbol == null) {
            throw new IllegalArgumentException("Error: symbol cannot be null");
        }

        try (PreparedStatement stmt = connection.prepareStatement(DELETE_BY_ID_SQL)) {
            stmt.setString(1, symbol);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting Position by symbol", e);
        }
    }

    /**
     * Deletes all Positions from the database.
     */
    @Override
    public void deleteAll() {
        try (PreparedStatement stmt = connection.prepareStatement(DELETE_ALL_SQL)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting all Positions", e);
        }
    }

    // helper method for findByID to map ResultSet row to position object
    private Position mapRow(ResultSet rs) throws SQLException {
        Position position = new Position();
        position.setTicker(rs.getString("symbol"));
        position.setNumOfShares(rs.getInt("number_of_shares"));
        position.setValuePaid(rs.getDouble("value_paid"));
        return position;
    }
}
