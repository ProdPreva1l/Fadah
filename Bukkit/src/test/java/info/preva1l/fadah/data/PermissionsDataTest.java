package info.preva1l.fadah.data;

import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class PermissionsDataTest {
    private static final int DEFAULT_MAX_LISTINGS = 3;

    private static final Player player = new Player(List.of(
            new PermissionAttachmentInfo("fadah.max-listings.3"),
            new PermissionAttachmentInfo("fadah.max-listings.34"),
            new PermissionAttachmentInfo("fadah.use"),
            new PermissionAttachmentInfo("essentials.sucks")));
    private static final PermissionType type = PermissionType.MAX_LISTINGS;

    @Test
    public void valueFromPermission() {
        int currentMax = 0;

        for (PermissionAttachmentInfo effectivePermission : player.getEffectivePermissions()) {
            if (!effectivePermission.getPermission().startsWith(type.permissionString)) continue;
            String numberStr = effectivePermission.getPermission().substring(type.permissionString.length());
            try {
                if (currentMax < Integer.parseInt(numberStr)) currentMax = Integer.parseInt(numberStr);
            } catch (NumberFormatException ignored) {
                currentMax = DEFAULT_MAX_LISTINGS;
            }
        }

        Assertions.assertEquals(34, currentMax);
    }

    @AllArgsConstructor
    public enum PermissionType {
        MAX_LISTINGS("fadah.max-listings."),
        ;
        private final String permissionString;
    }

    private record PermissionAttachmentInfo(String getPermission) {}
    private record Player(List<PermissionAttachmentInfo> getEffectivePermissions) {}
}