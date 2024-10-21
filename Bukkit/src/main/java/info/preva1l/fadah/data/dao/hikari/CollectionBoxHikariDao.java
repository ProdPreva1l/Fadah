package info.preva1l.fadah.data.dao.hikari;

import com.google.common.collect.Lists;
import info.preva1l.fadah.data.DatabaseType;
import info.preva1l.fadah.data.dao.SqlDao;
import info.preva1l.fadah.data.handler.HikariHandler;
import info.preva1l.fadah.records.CollectableItem;
import info.preva1l.fadah.records.CollectionBox;
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

public class CollectionBoxHikariDao extends SqlDao<CollectionBox> {

    public CollectionBoxHikariDao(@NotNull HikariHandler handler) {
        super("collection box", handler);

        statement(Statement.SELECT, """
                        SELECT `uuid`, `owner_id`, `item`, `update`
                        FROM `items`
                        WHERE `buyer_id` = ? AND `collected` = ?;
                        """);
        statement(Statement.INSERT, """
                        INSERT INTO `items` (`uuid`, `owner_id`, `buyer_id`, `item`, `time`, `update`, `collected`)
                        VALUES (?, ?, ?, ?, ?, ?, ?)
                        ON DUPLICATE KEY UPDATE `buyer_id` = VALUES(`buyer_id`), `item` = VALUES(`item`), `update` = VALUES(`update`), `collected` = VALUES(`collected`);
                        """,
                DatabaseType.POSTGRESQL, """
                        INSERT INTO `items` (`uuid`, `owner_id`, `buyer_id`, `item`, `time`, `update`, `collected`)
                        VALUES (?, ?, ?, ?, ?, ?, ?)
                        ON CONFLICT (`uuid`) DO UPDATE SET `buyer_id` = EXCLUDED.`buyer_id`, `item` = EXCLUDED.`item`, `update` = EXCLUDED.`update`, `collected` = EXCLUDED.`collected`;
                        """,
                DatabaseType.SQLITE, """
                        INSERT OR REPLACE INTO `items` (`uuid`, `owner_id`, `buyer_id`, `item`, `time`, `update`, `collected`)
                        VALUES (?, ?, ?, ?, ?, ?, ?);
                        """,
                DatabaseType.H2, """
                        MERGE INTO `items` (`uuid`, `owner_id`, `buyer_id`, `item`, `time`, `update`, `collected`)
                        KEY (`uuid`)
                        VALUES (?, ?, ?, ?, ?, ?, ?);
                        """);
        statement(Statement.DELETE_SPECIFIC, """
                        UPDATE `items` SET
                        `collected` = ?
                        WHERE `uuid` = ?;
                        """);
    }

    @Override
    protected @Nullable CollectionBox select(UUID buyer, Connection con, PreparedStatement stmt) throws SQLException {
        final List<CollectableItem> retrievedData = Lists.newArrayList();

        stmt.setString(1, buyer.toString());
        stmt.setBoolean(2, false);

        final ResultSet result = stmt.executeQuery();
        while (result.next()) {
            final UUID id = UUID.fromString(result.getString("uuid"));
            final UUID ownerId = UUID.fromString(result.getString("owner_id"));
            final ItemStack itemStack = ItemSerializer.deserialize(result.getString("item"))[0];
            final long dateAdded = result.getLong("update");
            retrievedData.add(new CollectableItem(id, ownerId, itemStack, dateAdded));
        }
        return new CollectionBox(buyer, retrievedData);
    }

    @Override
    protected void insert(CollectionBox collectionBox, Connection con, PreparedStatement stmt) throws SQLException {
        final long time = Instant.now().minus(2, ChronoUnit.DAYS).toEpochMilli();
        for (CollectableItem item : collectionBox.collectableItems()) {
            stmt.setString(1, item.id().toString());
            stmt.setString(2, item.owner().toString());
            stmt.setString(3, collectionBox.owner().toString());
            stmt.setString(4, ItemSerializer.serialize(item.itemStack()));
            stmt.setLong(5, time);
            stmt.setLong(6, item.dateAdded());
            stmt.setBoolean(7, false);

            stmt.addBatch();
        }

        stmt.executeBatch();
    }

    @Override
    protected void deleteSpecific(CollectionBox collectionBox, Object o, Connection con, PreparedStatement stmt) throws SQLException {
        if (!(o instanceof CollectableItem item)) throw new IllegalStateException("Specific object must be a collectable item");
        stmt.setBoolean(1, true);
        stmt.setString(2, item.id().toString());
        stmt.execute();
    }
}
