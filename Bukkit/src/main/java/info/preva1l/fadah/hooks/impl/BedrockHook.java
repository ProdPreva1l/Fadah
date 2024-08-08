package info.preva1l.fadah.hooks.impl;

import info.preva1l.fadah.config.Config;
import lombok.experimental.UtilityClass;
import org.bukkit.entity.Player;
import org.geysermc.floodgate.api.FloodgateApi;

@UtilityClass
public class BedrockHook {
    private final FloodgateApi floodgate = FloodgateApi.getInstance();

    public boolean shouldShowBedrockMenu(Player player) {
        return isBedrockPlayer(player) && Config.HOOK_BEDROCK.toBoolean();
    }

    public boolean isBedrockPlayer(Player player) {
        return floodgate.isFloodgatePlayer(player.getUniqueId());
    }
}
