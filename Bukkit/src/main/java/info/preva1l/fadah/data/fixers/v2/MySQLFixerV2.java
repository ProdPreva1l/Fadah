package info.preva1l.fadah.data.fixers.v2;

import com.google.common.collect.Lists;
import com.zaxxer.hikari.HikariDataSource;
import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.data.DatabaseManager;
import info.preva1l.fadah.records.CollectableItem;
import info.preva1l.fadah.records.CollectionBox;
import info.preva1l.fadah.records.ExpiredItems;
import info.preva1l.fadah.utils.ItemSerializer;
import lombok.RequiredArgsConstructor;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class MySQLFixerV2 implements V2Fixer {
    private final HikariDataSource dataSource;

    @Override
    public void fixExpiredItems(UUID player) {
        final List<CollectableItem> retrievedData = Lists.newArrayList();
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("""
                        SELECT `itemStack`,  `dateAdded`
                        FROM `expired_items`
                        WHERE `playerUUID`=?;""")) {
                statement.setString(1, player.toString());
                final ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    final ItemStack itemStack = ItemSerializer.deserialize(resultSet.getString("itemStack"))[0];
                    final long dateAdded = resultSet.getLong("dateAdded");
                    retrievedData.add(new CollectableItem(itemStack, dateAdded));
                }
                DatabaseManager.getInstance().save(ExpiredItems.class, new ExpiredItems(player, retrievedData));
            }
        } catch (SQLException e) {
            Fadah.getConsole().severe("Failed to get items from collection box!");
            throw new RuntimeException(e);
        }
    }

    @Override
    public void fixCollectionBox(UUID player) {
        final List<CollectableItem> retrievedData = Lists.newArrayList();
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("""
                        SELECT `itemStack`,  `dateAdded`
                        FROM `collection_box`
                        WHERE `playerUUID`=?;""")) {
                statement.setString(1, player.toString());
                final ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    final ItemStack itemStack = ItemSerializer.deserialize(resultSet.getString("itemStack"))[0];
                    final long dateAdded = resultSet.getLong("dateAdded");
                    retrievedData.add(new CollectableItem(itemStack, dateAdded));
                }
                DatabaseManager.getInstance().save(CollectionBox.class, new CollectionBox(player, retrievedData));
            }
        } catch (SQLException e) {
            Fadah.getConsole().severe("Failed to get items from collection box!");
            throw new RuntimeException(e);
        }
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
