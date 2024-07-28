package info.preva1l.fadah.microservice.storage;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface Database {

    void connect();

    void destroy();

    CompletableFuture<Void> addToCollectionBox(UUID playerUUID, JsonObject collectableItem);

    CompletableFuture<Void> removeFromCollectionBox(UUID playerUUID, JsonObject collectableItem);

    CompletableFuture<List<JsonObject>> getCollectionBox(UUID playerUUID);

    CompletableFuture<Void> addToExpiredItems(UUID playerUUID, JsonObject collectableItem);

    CompletableFuture<Void> removeFromExpiredItems(UUID playerUUID, JsonObject collectableItem);

    CompletableFuture<List<JsonObject>> getExpiredItems(UUID playerUUID);

    CompletableFuture<Void> addListing(JsonObject listing);

    CompletableFuture<Void> removeListing(UUID id);

    CompletableFuture<List<JsonObject>> getListings();

    CompletableFuture<@Nullable JsonObject> getListing(UUID id);

    CompletableFuture<Void> addToHistory(UUID playerUUID, JsonObject historicItem);

    CompletableFuture<List<JsonObject>> getHistory(UUID playerUUID);

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean isConnected();
}