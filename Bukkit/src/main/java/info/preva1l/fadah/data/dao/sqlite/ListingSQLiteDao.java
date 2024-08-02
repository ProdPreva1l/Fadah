package info.preva1l.fadah.data.dao.sqlite;

import com.zaxxer.hikari.HikariDataSource;
import info.preva1l.fadah.data.dao.Dao;
import info.preva1l.fadah.records.Listing;
import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class ListingSQLiteDao implements Dao<Listing> {
    private final HikariDataSource dataSource;

    /**
     * Get an object from the database by its id.
     *
     * @param id the id of the object to get.
     * @return an optional containing the object if it exists, or an empty optional if it does not.
     */
    @Override
    public Optional<Listing> get(UUID id) {
        return Optional.empty();
    }

    /**
     * Get all objects of type T from the database.
     *
     * @return a list of all objects of type T in the database.
     */
    @Override
    public List<Listing> getAll() {
        List<Listing> listings = new ArrayList<>();
        String sql = "SELECT * FROM player";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                players.add(mapResultSetToPlayer(rs));
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
        return players;
    }

    /**
     * Save an object of type T to the database.
     *
     * @param listing the object to save.
     */
    @Override
    public void save(Listing listing) {

    }

    /**
     * Update an object of type T in the database.
     *
     * @param listing the object to update.
     * @param params  the parameters to update the object with.
     */
    @Override
    public void update(Listing listing, String[] params) {

    }

    /**
     * Delete an object of type T from the database.
     *
     * @param listing the object to delete.
     */
    @Override
    public void delete(Listing listing) {

    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
