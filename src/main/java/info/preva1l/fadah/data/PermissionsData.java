package info.preva1l.fadah.data;

import info.preva1l.fadah.cache.ListingCache;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.records.Listing;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.List;


@UtilityClass
public class PermissionsData {
    public int getCurrentListings(Player player) {
        List<Listing> listings = ListingCache.getListings();
        listings.removeIf(listing -> !listing.isOwner(player));
        return listings.size();
    }

    public int getMaxListings(Player player) {
        String perm = getPermission(PermissionType.MAX_LISTINGS, player);
        if (perm == null) {
            return Config.DEFAULT_MAX_LISTINGS.toInteger();
        }
        Config.DEFAULT_MAX_LISTINGS.toInteger();
        int num;
        try {
            num = Integer.parseInt(perm);
        } catch (NumberFormatException e) {
            num = Config.DEFAULT_MAX_LISTINGS.toInteger();
        }

        return num;
    }


    private String getPermission(PermissionType type, Player player) {
        for (PermissionAttachmentInfo effectivePermission : player.getEffectivePermissions()) {
            if (effectivePermission.getPermission().startsWith(type.permissionString)) {
                return effectivePermission.getPermission();
            }
        }
        return null;
    }

    @Getter
    @AllArgsConstructor
    private enum PermissionType {
        MAX_LISTINGS("fadah.maxlistings"),
        ;
        private final String permissionString;
    }
}
