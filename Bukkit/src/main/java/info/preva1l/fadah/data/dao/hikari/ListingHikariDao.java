package info.preva1l.fadah.data.dao.hikari;

import info.preva1l.fadah.data.dao.SqlDao;
import info.preva1l.fadah.data.handler.HikariHandler;
import info.preva1l.fadah.records.CurrentListing;
import info.preva1l.fadah.records.Listing;
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
import java.util.concurrent.TimeUnit;

public class ListingHikariDao extends SqlDao<Listing> {

    public ListingHikariDao(@NotNull HikariHandler handler) {
        super("listing", handler);

        statement(Statement.SELECT, """
                        SELECT `owner_id`, `owner_name`, `item`, `category`, `price`, `tax`, `time`, `biddable`, `bids`
                        FROM `items`
                        WHERE `uuid`= ? AND `time` > ? AND `buyer_id` IS NULL;
                        """);
        statement(Statement.SELECT_ALL, """
                        SELECT `uuid`, `owner_id`, `owner_name`, `item`, `category`, `price`, `tax`, `time`, `biddable`, `bids`
                        FROM `items`
                        WHERE `time` > ? AND `buyer_id` IS NULL;
                        """);
        statement(Statement.INSERT, """
                        INSERT INTO `items`
                        (`uuid`, `owner_id`, `owner_name`, `item`, `category`, `price`, `tax`, `time`, `biddable`, `bids`, `update`)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
                        """);
        statement(Statement.DELETE, """
                        DELETE FROM `items`
                        WHERE uuid = ?;
                        """);
    }

    @Override
    protected @Nullable Listing select(UUID id, Connection con, PreparedStatement stmt) throws SQLException {
        stmt.setString(1, id.toString());
        stmt.setLong(2, Instant.now().minus(2, ChronoUnit.DAYS).toEpochMilli());

        CurrentListing listing = null;

        final ResultSet result = stmt.executeQuery();
        if (result.next()) {
            final UUID ownerId = UUID.fromString(result.getString("owner_id"));
            final String ownerName = result.getString("owner_name");
            final ItemStack itemStack = ItemSerializer.deserialize(result.getString("item"))[0];
            final String category = result.getString("category");
            final double price = result.getDouble("price");
            final double tax = result.getDouble("tax");
            final long creationDate = result.getLong("time");
            final long deletionDate = creationDate + TimeUnit.DAYS.toMillis(2);
            final boolean biddable = result.getBoolean("biddable");
            listing = new CurrentListing(id, ownerId, ownerName, itemStack, category, price, tax, creationDate, deletionDate, biddable, List.of());
        }
        return listing;
    }

    @Override
    protected List<Listing> selectAll(Connection con, PreparedStatement stmt) throws SQLException {
        stmt.setLong(1, Instant.now().minus(2, ChronoUnit.DAYS).toEpochMilli());

        final List<Listing> list = new ArrayList<>();

        final ResultSet result = stmt.executeQuery();
        while (result.next()) {
            final UUID id = UUID.fromString(result.getString("uuid"));
            final UUID ownerId = UUID.fromString(result.getString("owner_id"));
            final String ownerName = result.getString("owner_name");
            final ItemStack itemStack = ItemSerializer.deserialize(result.getString("item"))[0];
            final String categoryID = result.getString("category");
            final double price = result.getDouble("price");
            final double tax = result.getDouble("tax");
            final long creationDate = result.getLong("time");
            final long deletionDate = creationDate + TimeUnit.DAYS.toMillis(2);
            final boolean biddable = result.getBoolean("biddable");
            list.add(new CurrentListing(id, ownerId, ownerName, itemStack, categoryID, price, tax, creationDate, deletionDate, biddable, List.of()));
        }
        return list;
    }

    @Override
    protected void insert(Listing listing, Connection con, PreparedStatement stmt) throws SQLException {
        stmt.setString(1, listing.getId().toString());
        stmt.setString(2, listing.getOwner().toString());
        stmt.setString(3, listing.getOwnerName());
        stmt.setString(4, ItemSerializer.serialize(listing.getItemStack()));
        stmt.setString(5, listing.getCategoryID());
        stmt.setDouble(6, listing.getPrice());
        stmt.setDouble(7, listing.getTax());
        stmt.setLong(8, listing.getCreationDate());
        stmt.setBoolean(9, false);
        stmt.setString(10, "");
        stmt.setLong(11, Instant.now().toEpochMilli());
        stmt.execute();
    }

    @Override
    protected void delete(Listing listing, Connection con, PreparedStatement stmt) throws SQLException {
        stmt.setString(1, listing.getId().toString());
        stmt.execute();
    }
}
