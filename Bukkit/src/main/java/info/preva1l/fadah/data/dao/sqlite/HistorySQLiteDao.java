package info.preva1l.fadah.data.dao.sqlite;

import com.google.common.collect.Lists;
import com.zaxxer.hikari.HikariDataSource;
import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.data.dao.Dao;
import info.preva1l.fadah.records.HistoricItem;
import info.preva1l.fadah.records.History;
import info.preva1l.fadah.utils.ItemSerializer;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.NotImplementedException;
import org.bukkit.inventory.ItemStack;

import java.sql.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class HistorySQLiteDao implements Dao<History> {
    private final HikariDataSource dataSource;

    /**
     * Get an object from the database by its id.
     *
     * @param id the id of the object to get.
     * @return an optional containing the object if it exists, or an empty optional if it does not.
     */
    @Override
    public Optional<History> get(UUID id) {
        final List<HistoricItem> retrievedData = Lists.newArrayList();
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("""
                        SELECT `itemStack`, `loggedDate`, `loggedAction`, `price`, `purchaserUUID`
                        FROM `history`
                        WHERE `playerUUID`=?;""")) {
                statement.setString(1, id.toString());
                final ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    final long loggedDate = resultSet.getLong("loggedDate");
                    final double price = resultSet.getDouble("price");
                    final ItemStack itemStack = ItemSerializer.deserialize(resultSet.getString("itemStack"))[0];
                    final HistoricItem.LoggedAction loggedAction = HistoricItem.LoggedAction.values()[resultSet.getInt("loggedAction")];
                    final UUID purchaserUUID = resultSet.getString("purchaserUUID") == null ? null : UUID.fromString(resultSet.getString("purchaserUUID"));
                    retrievedData.add(new HistoricItem(id, loggedDate, loggedAction, itemStack, price, purchaserUUID));
                }
                return Optional.of(new History(id, retrievedData));
            }
        } catch (SQLException e) {
            Fadah.getConsole().severe("Failed to get items from collection box!");
        }
        return Optional.empty();
    }

    /**
     * Get all objects of type T from the database.
     *
     * @return a list of all objects of type T in the database.
     */
    @Override
    public List<History> getAll() {
        throw new NotImplementedException();
    }

    /**
     * Save an object of type T to the database.
     *
     * @param history the object to save.
     */
    @Override
    public void save(History history) {
        try (Connection connection = getConnection()) {
            for (HistoricItem historicItem : history.collectableItems()) {
                try (PreparedStatement statement = connection.prepareStatement("""
                        INSERT INTO `history`
                        (`playerUUID`, `itemStack`, `loggedDate`, `loggedAction`, `price`, `purchaserUUID`)
                        VALUES (?,?,?,?,?,?);""")) {
                    statement.setString(1, history.owner().toString());
                    statement.setString(2, ItemSerializer.serialize(historicItem.itemStack()));
                    statement.setLong(3, historicItem.loggedDate());
                    statement.setInt(4, historicItem.action().ordinal());
                    if (historicItem.price() != null) {
                        statement.setDouble(5, historicItem.price());
                    } else {
                        statement.setNull(5, Types.DOUBLE);
                    }
                    if (historicItem.purchaserUUID() != null) {
                        statement.setString(6, historicItem.purchaserUUID().toString());
                    } else {
                        statement.setNull(6, Types.VARCHAR);
                    }
                    statement.execute();
                }
            }
        } catch (SQLException e) {
            Fadah.getConsole().severe("Failed to add item to collection box!");
            throw new RuntimeException(e);
        }
    }

    /**
     * Update an object of type T in the database.
     *
     * @param history the object to update.
     * @param params          the parameters to update the object with.
     */
    @Override
    public void update(History history, String[] params) {
        throw new NotImplementedException();
    }

    /**
     * Delete an object of type T from the database.
     *
     * @param collectableItem the object to delete.
     */
    @Override
    public void delete(History collectableItem) {
        throw new NotImplementedException();
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
