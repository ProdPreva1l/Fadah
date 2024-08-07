package info.preva1l.fadah.utils;

import lombok.experimental.UtilityClass;
import org.bukkit.entity.Player;
import org.geysermc.floodgate.api.FloodgateApi;

@UtilityClass
public class BedrockUtils {
    private final FloodgateApi floodgate = FloodgateApi.getInstance();

    public boolean isBedrockPlayer(Player player) {
        return floodgate.isFloodgatePlayer(player.getUniqueId());
    }
}
