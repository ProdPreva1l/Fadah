package info.preva1l.fadah.data.dao.hikari;

import info.preva1l.fadah.data.DatabaseType;
import info.preva1l.fadah.data.dao.SqlDao;
import info.preva1l.fadah.data.handler.DataHandler;
import info.preva1l.fadah.data.handler.HikariHandler;
import info.preva1l.fadah.records.HistoricItem;
import info.preva1l.fadah.records.History;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HistoryHikariDao extends SqlDao<History> {

    public HistoryHikariDao(@NotNull HikariHandler handler) {
        super("history", handler);

        statement(Statement.SELECT, """
                    SELECT `uuid`, `owner_id`, `buyer_id`, `item`, `price`, `time`, `update`, `collected`
                    FROM `items`
                    WHERE `owner_id`= ? OR `buyer_id`= ?;
                    """);
        statement(Statement.INSERT, """
                    INSERT INTO `items` (`uuid`, `owner_id`, `buyer_id`, `item`, `price`, `time`, `update`, `collected`)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                    ON DUPLICATE KEY UPDATE `item` = VALUES(`item`), `update` = VALUES(`update`);
                    """,
                DatabaseType.POSTGRESQL, """
                    INSERT INTO `items` (`uuid`, `owner_id`, `buyer_id`, `item`, `price`, `time`, `update`, `collected`)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                    ON CONFLICT (`uuid`) DO UPDATE SET `item` = EXCLUDED.`item`, `update` = EXCLUDED.`update`;
                    """,
                DatabaseType.SQLITE, """
                    INSERT OR REPLACE INTO `items` (`uuid`, `owner_id`, `buyer_id`, `item`, `price`, `time`, `update`, `collected`)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?);
                    """,
                DatabaseType.H2, """
                    MERGE INTO `items` (`uuid`, `owner_id`, `buyer_id`, `item`, `price`, `time`, `update`, `collected`)
                    KEY (`uuid`)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?);
                    """);
    }

    @Override
    protected @Nullable History select(UUID owner, Connection con, PreparedStatement stmt) throws SQLException {
        stmt.setString(1, owner.toString());
        stmt.setString(2, owner.toString());

        final long expiry = Instant.now().minus(2, ChronoUnit.DAYS).toEpochMilli();
        final List<HistoricItem> items = new ArrayList<>();

        final ResultSet result = stmt.executeQuery();
        while (result.next()) {
            final UUID id = UUID.fromString(result.getString("uuid"));
            final UUID ownerId = UUID.fromString(result.getString("owner_id"));
            final String buyer = result.getString("buyer_id");
            final UUID buyerId = buyer == null ? null : UUID.fromString(buyer);
            final ItemStack itemStack = ItemSerializer.deserialize(result.getString("item"))[0];
            final long time = result.getLong("time");
            final double price = result.getDouble("price");
            final long update = result.getLong("update");
            final boolean collected = result.getBoolean("collected");

            final HistoricItem.LoggedAction action;
            if (ownerId.equals(owner)) {
                if (buyerId == null) {
                    if (time < expiry) {
                        action = HistoricItem.LoggedAction.LISTING_EXPIRE;
                    } else {
                        action = HistoricItem.LoggedAction.LISTING_START;
                    }
                } else if (buyerId == DataHandler.DUMMY_ID) {
                    action = HistoricItem.LoggedAction.LISTING_CANCEL;
                } else {
                    action = HistoricItem.LoggedAction.LISTING_SOLD;
                }
            } else {
                if (collected) {
                    action = HistoricItem.LoggedAction.COLLECTION_BOX_CLAIM;
                } else {
                    action = HistoricItem.LoggedAction.LISTING_PURCHASED;
                }
            }

            final HistoricItem historicItem = new HistoricItem(id, ownerId, update, action, itemStack, price, buyerId);
            historicItem.setSaved(true);
            items.add(historicItem);
        }
        return new History(owner, items);
    }

    @Override
    protected void insert(History history, Connection con, PreparedStatement stmt) throws SQLException {
        for (HistoricItem item : history.collectableItems()) {
            if (item.isSaved()) {
                continue;
            }
            item.setSaved(true);

            stmt.setString(1, item.getId().toString());
            stmt.setString(2, item.getOwnerUUID().toString());
            stmt.setString(3, item.getPurchaserUUID() == null ? null : item.getPurchaserUUID().toString());
            stmt.setString(4, ItemSerializer.serialize(item.getItemStack()));
            stmt.setDouble(5, item.getPrice() == null ? 0 : item.getPrice());
            stmt.setLong(6, -1);
            stmt.setLong(7, item.getLoggedDate());
            stmt.setBoolean(8, item.getAction() == HistoricItem.LoggedAction.EXPIRED_ITEM_CLAIM
                    || item.getAction() == HistoricItem.LoggedAction.EXPIRED_ITEM_ADMIN_CLAIM
                    || item.getAction() == HistoricItem.LoggedAction.COLLECTION_BOX_CLAIM
                    || item.getAction() == HistoricItem.LoggedAction.COLLECTION_BOX_ADMIN_CLAIM
            );

            stmt.addBatch();
        }
        stmt.executeBatch();
    }
}
