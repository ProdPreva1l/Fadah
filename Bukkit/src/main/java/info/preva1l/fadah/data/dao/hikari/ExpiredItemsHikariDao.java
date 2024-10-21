package info.preva1l.fadah.data.dao.hikari;

import com.google.common.collect.Lists;
import info.preva1l.fadah.data.DatabaseType;
import info.preva1l.fadah.data.dao.SqlDao;
import info.preva1l.fadah.data.handler.HikariHandler;
import info.preva1l.fadah.records.CollectableItem;
import info.preva1l.fadah.records.ExpiredItems;
import info.preva1l.fadah.utils.ItemSerializer;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

public class ExpiredItemsHikariDao extends SqlDao<ExpiredItems> {

    public ExpiredItemsHikariDao(@NotNull HikariHandler handler) {
        super("expired items", handler);

        statement(Statement.SELECT, """
                        SELECT `uuid`, `owner_id`, `item`, `update`
                        FROM `items`
                        WHERE `owner_id` = ? AND `time` < ? AND `collected` = ? AND `buyer_id` IS NULL;
                        """);
        statement(Statement.INSERT, """
                        INSERT INTO `items` (`uuid`, `owner_id`, `item`, `time`, `update`, `collected`)
                        VALUES (?, ?, ?, ?, ?, ?)
                        ON DUPLICATE KEY UPDATE `item` = VALUES(`item`), `time` = VALUES(`time`), `update` = VALUES(`update`), `collected` = VALUES(`collected`);
                        """,
                DatabaseType.SQLITE, """
                        INSERT OR REPLACE INTO `items` (`uuid`, `owner_id`, `item`, `time`, `update`, `collected`)
                        VALUES (?, ?, ?, ?, ?, ?);
                        """);
        statement(Statement.DELETE_SPECIFIC, """
                        UPDATE `items` SET
                        `collected` = ?
                        WHERE `uuid` = ?;
                        """);
    }

    @Override
    protected @Nullable ExpiredItems select(UUID owner, Connection con, PreparedStatement stmt) throws SQLException {
        final List<CollectableItem> retrievedData = Lists.newArrayList();

        stmt.setString(1, owner.toString());
        stmt.setLong(2, Instant.now().minus(2, ChronoUnit.DAYS).toEpochMilli());
        stmt.setBoolean(3, false);

        final ResultSet result = stmt.executeQuery();
        while (result.next()) {
            final UUID id = UUID.fromString(result.getString("uuid"));
            final UUID ownerId = UUID.fromString(result.getString("owner_id"));
            final ItemStack itemStack = ItemSerializer.deserialize(result.getString("item"))[0];
            final long dateAdded = result.getLong("update");
            retrievedData.add(new CollectableItem(id, ownerId, itemStack, dateAdded));
        }
        return new ExpiredItems(owner, retrievedData);
    }

    @Override
    protected void insert(ExpiredItems expiredItems, Connection con, PreparedStatement stmt) throws SQLException {
        final long time = Instant.now().minus(2, ChronoUnit.DAYS).toEpochMilli();
        for (CollectableItem item : expiredItems.collectableItems()) {
            stmt.setString(1, item.id().toString());
            stmt.setString(2, expiredItems.owner().toString());
            stmt.setString(3, ItemSerializer.serialize(item.itemStack()));
            stmt.setLong(4, time);
            stmt.setLong(5, item.dateAdded());
            stmt.setBoolean(6, false);

            stmt.addBatch();
        }
        stmt.executeBatch();
    }

    @Override
    protected void deleteSpecific(ExpiredItems expiredItems, Object o, Connection con, PreparedStatement stmt) throws SQLException {
        if (!(o instanceof CollectableItem item)) throw new IllegalStateException("Specific object must be a collectable item");
        stmt.setBoolean(1, true);
        stmt.setString(2, item.id().toString());
        stmt.execute();
    }
}
