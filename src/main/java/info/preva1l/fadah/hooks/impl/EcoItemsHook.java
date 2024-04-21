package info.preva1l.fadah.hooks.impl;

import com.willfp.ecoitems.items.EcoItem;
import com.willfp.ecoitems.items.EcoItems;
import info.preva1l.fadah.hooks.Hook;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;

public class EcoItemsHook implements Hook {
    @Getter @Setter private boolean enabled = false;

    @Override
    public void enable() {
        setEnabled(true);
    }

    public boolean isEcoItem(ItemStack item) {
        for (EcoItem ecoItem : EcoItems.INSTANCE.values()) {
            if (item.getItemMeta().getPersistentDataContainer().getKeys().contains(ecoItem.getId())) return true;
        }
        return false;
    }
}