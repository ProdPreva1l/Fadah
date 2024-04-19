package info.preva1l.fadah.data;

import info.preva1l.fadah.cache.ListingCache;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.records.Listing;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.List;

@UtilityClass
public class PermissionsData {
    public int getCurrentListings(Player player) {
        List<Listing> listings = ListingCache.getListings();
        listings.removeIf(listing -> !listing.isOwner(player));
        return listings.size();
    }

    public int valueFromPermission(PermissionType type, Player player) {
        int currentMax = 0;
        boolean matched = false;
        for (PermissionAttachmentInfo effectivePermission : player.getEffectivePermissions()) {
            if (!effectivePermission.getPermission().startsWith(type.permissionString)) continue;
            String numberStr = effectivePermission.getPermission().substring(type.permissionString.length());
            try {
                if (currentMax < Integer.parseInt(numberStr)) {
                    currentMax = Integer.parseInt(numberStr);
                    matched = true;
                }
            } catch (NumberFormatException ignored) {}
        }
        return matched ? currentMax : Config.DEFAULT_MAX_LISTINGS.toInteger();
    }

    @AllArgsConstructor
    public enum PermissionType {
        MAX_LISTINGS("fadah.max-listings."),
        ;
        private final String permissionString;
    }
}
