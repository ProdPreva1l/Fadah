package info.preva1l.fadah.migrator;

import fr.maxlego08.zauctionhouse.api.AuctionItem;
import fr.maxlego08.zauctionhouse.api.AuctionManager;
import fr.maxlego08.zauctionhouse.api.AuctionPlugin;
import fr.maxlego08.zauctionhouse.api.category.Category;
import fr.maxlego08.zauctionhouse.api.category.CategoryManager;
import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.cache.CategoryCache;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.records.CollectableItem;
import info.preva1l.fadah.records.CurrentListing;
import info.preva1l.fadah.records.Listing;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Getter
public final class zAuctionHouseMigrator implements Migrator {
    private final String migratorName = "zAuctionHouse";
    private final AuctionManager auctionManager;
    private final CategoryManager categoryManager;

    public zAuctionHouseMigrator() {
        auctionManager = getProvider(AuctionManager.class);
        categoryManager = getProvider(CategoryManager.class);
    }

    @Override
    public CompletableFuture<Void> startMigration(Fadah plugin) {
        auctionManager.getStorage().save((AuctionPlugin) Bukkit.getPluginManager().getPlugin("zAuctionHouse"));
        return Migrator.super.startMigration(plugin);
    }

    @Override
    public List<Listing> migrateListings() {
        List<Listing> listings = new ArrayList<>();
        for (String categoryName : Config.i().getMigrators().getZAuctionHouse().getCategoriesToMigrate()) {
            Optional<Category> zCategory = categoryManager.getByName(categoryName);
            zCategory.ifPresentOrElse((category) -> {
                List<AuctionItem> zListings = auctionManager.getItems(category);
                for (AuctionItem auctionItem : zListings) {
                    UUID id = auctionItem.getUniqueId();
                    UUID owner = auctionItem.getSellerUniqueId();
                    String ownerName = auctionItem.getSellerName();
                    ItemStack itemStack = auctionItem.getItemStack();
                    double price = auctionItem.getPrice();
                    long expiry = auctionItem.getExpireAt();
                    String categoryId = CategoryCache.getCategoryForItem(itemStack);
                    if (categoryId == null) {
                        categoryId = CategoryCache.getCategories().get(0).id();
                    }
                    listings.add(new CurrentListing(id, owner, ownerName, itemStack, categoryId, price, 0,
                            Instant.now().toEpochMilli(), expiry, false, List.of()));
                }
            }, () -> Fadah.getConsole().warning("Not migrating category %s! (Not found on zAuctionHouse)".formatted(categoryName)));
        }
        return listings;
    }

    @Override
    public Map<UUID, List<CollectableItem>> migrateCollectionBoxes() {
        Fadah.getConsole().warning("Not migrating collection boxes! (zAuctionHouse API does not permit)");
        return Collections.emptyMap();
    }

    @Override
    public Map<UUID, List<CollectableItem>> migrateExpiredListings() {
        Fadah.getConsole().warning("Not migrating expired listings! (zAuctionHouse API does not permit)");
        return Collections.emptyMap();
    }

    private <T> T getProvider(Class<T> clazz) {
        RegisteredServiceProvider<T> provider = Bukkit.getServicesManager().getRegistration(clazz);
        if (provider == null) return null;
        return provider.getProvider() != null ? (T) provider.getProvider() : null;
    }
}
