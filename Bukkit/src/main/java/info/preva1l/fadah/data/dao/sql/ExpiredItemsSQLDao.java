package info.preva1l.fadah.data.dao.sql;

import com.google.common.collect.Lists;
import com.zaxxer.hikari.HikariDataSource;
import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.data.dao.Dao;
import info.preva1l.fadah.records.CollectableItem;
import info.preva1l.fadah.records.ExpiredItems;
import info.preva1l.fadah.utils.ItemSerializer;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.NotImplementedException;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

@RequiredArgsConstructor
public class ExpiredItemsSQLDao implements Dao<ExpiredItems> {
    private final HikariDataSource dataSource;

    /**
     * Get an object from the database by its id.
     *
     * @param id the id of the object to get.
     * @return an optional containing the object if it exists, or an empty optional if it does not.
     */
    @Override
    public Optional<ExpiredItems> get(UUID id) {
        final List<CollectableItem> retrievedData = Lists.newArrayList();
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("""
                        SELECT `itemStack`,  `dateAdded`
                        FROM `expired_items`
                        WHERE `playerUUID`=?;""")) {
                statement.setString(1, id.toString());
                final ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    final ItemStack itemStack = ItemSerializer.deserialize(resultSet.getString("itemStack"))[0];
                    final long dateAdded = resultSet.getLong("dateAdded");
                    retrievedData.add(new CollectableItem(itemStack, dateAdded));
                }
                return Optional.of(new ExpiredItems(id, retrievedData));
            }
        } catch (SQLException e) {
            Fadah.getConsole().severe("Failed to get items from collection box!");
            throw new RuntimeException(e);
        }
    }

    /**
     * Get all objects of type T from the database.
     *
     * @return a list of all objects of type T in the database.
     */
    @Override
    public List<ExpiredItems> getAll() {
        throw new NotImplementedException();
    }

    /**
     * Save an object of type T to the database.
     *
     * @param collectableList the object to save.
     */
    @Override
    public void save(ExpiredItems collectableList) {
        try (Connection connection = getConnection()) {
            for (CollectableItem item : collectableList.collectableItems()) {
                try (PreparedStatement statement = connection.prepareStatement("""
                        INSERT INTO `expired_items` (`playerUUID`, `itemStack`, `dateAdded`)
                        SELECT ?, ?, ?
                        WHERE NOT EXISTS ( SELECT 1 FROM `expired_items` WHERE `dateAdded` = ?
                        );""")) {
                    statement.setString(1, collectableList.owner().toString());
                    statement.setString(2, ItemSerializer.serialize(item.itemStack()));
                    statement.setLong(3, item.dateAdded());
                    statement.setLong(4, item.dateAdded());
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
     * @param collectableItem the object to update.
     * @param params          the parameters to update the object with.
     */
    @Override
    public void update(ExpiredItems collectableItem, String[] params) {
        throw new NotImplementedException();
    }

    /**
     * Delete an object of type T from the database.
     *
     * @param collectableItem the object to delete.
     */
    @Override
    public void delete(ExpiredItems collectableItem) {
        throw new NotImplementedException();
    }

    @Override
    public void deleteSpecific(ExpiredItems collectableList, Object o) {
        if (!(o instanceof CollectableItem item)) throw new IllegalStateException("Specific object must be a collectable item");
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("""
                        DELETE FROM `expired_items`
                        WHERE `playerUUID`=? AND `itemStack`=? AND `dateAdded` =?;""")) {
                statement.setString(1, collectableList.owner().toString());
                statement.setString(2, ItemSerializer.serialize(item.itemStack()));
                statement.setLong(3, item.dateAdded());
                statement.execute();
            }
        } catch (SQLException e) {
            Fadah.getConsole().log(Level.SEVERE, "Failed to remove item from expired items!", e);
        }
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
