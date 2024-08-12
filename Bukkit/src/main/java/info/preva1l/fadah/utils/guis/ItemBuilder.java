package info.preva1l.fadah.utils.guis;

import com.google.common.collect.Multimap;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Simple {@link ItemStack} builder.
 *
 * @author MrMicky
 * @author Preva1l
 */
@SuppressWarnings({"unused", "deprecation"})
public class ItemBuilder {

    private final ItemStack item;

    public ItemBuilder(Material material) {
        this(new ItemStack(material));
    }

    public ItemBuilder(ItemStack item) {
        this.item = Objects.requireNonNull(item, "item");
    }

    public static ItemBuilder copyOf(ItemStack item) {
        return new ItemBuilder(item.clone());
    }

    public ItemBuilder edit(Consumer<ItemStack> function) {
        function.accept(this.item);
        return this;
    }

    public ItemBuilder meta(Consumer<ItemMeta> metaConsumer) {
        return edit(item -> {
            ItemMeta meta = item.getItemMeta();

            if (meta != null) {
                metaConsumer.accept(meta);
                item.setItemMeta(meta);
            }
        });
    }

    public <T extends ItemMeta> ItemBuilder meta(Class<T> metaClass, Consumer<T> metaConsumer) {
        return meta(meta -> {
            if (metaClass.isInstance(meta)) {
                metaConsumer.accept(metaClass.cast(meta));
            }
        });
    }

    public ItemBuilder type(Material material) {
        return edit(item -> item.setType(material));
    }

    public ItemBuilder durability(int data) {
        return durability((short) data);
    }

    @SuppressWarnings("deprecation")
    public ItemBuilder durability(short durability) {
        return edit(item -> item.setDurability(durability));
    }

    public ItemBuilder amount(int amount) {
        return edit(item -> item.setAmount(amount));
    }

    public void enchant(Enchantment enchantment) {
        enchant(enchantment, 1);
    }

    public void enchant(Enchantment enchantment, int level) {
        meta(meta -> meta.addEnchant(enchantment, level, true));
    }

    public ItemBuilder removeEnchant(Enchantment enchantment) {
        return meta(meta -> meta.removeEnchant(enchantment));
    }

    public ItemBuilder removeEnchants() {
        return meta(m -> m.getEnchants().keySet().forEach(m::removeEnchant));
    }

    public ItemBuilder name(String name) {
        return meta(meta -> meta.setDisplayName(name));
    }

    public ItemBuilder lore(String lore) {
        return lore(Collections.singletonList(lore));
    }

    public ItemBuilder lore(String... lore) {
        return lore(Arrays.asList(lore));
    }

    public ItemBuilder lore(List<String> lore) {
        return meta(meta -> meta.setLore(lore));
    }

    public ItemBuilder addLore(String line) {
        return meta(meta -> {
            List<String> lore = meta.getLore();

            if (lore == null) {
                meta.setLore(Collections.singletonList(line));
                return;
            }

            lore.add(line);
            meta.setLore(lore);
        });
    }

    public ItemBuilder addLore(String... lines) {
        return addLore(Arrays.asList(lines));
    }

    public ItemBuilder addLore(List<String> lines) {
        return meta(meta -> {
            List<String> lore = meta.getLore();

            if (lore == null) {
                meta.setLore(lines);
                return;
            }

            lore.addAll(lines);
            meta.setLore(lore);
        });
    }

    public ItemBuilder flags(ItemFlag... flags) {
        return meta(meta -> meta.addItemFlags(flags));
    }

    public ItemBuilder flags() {
        return flags(ItemFlag.values());
    }

    public ItemBuilder removeFlags(ItemFlag... flags) {
        return meta(meta -> meta.removeItemFlags(flags));
    }

    public ItemBuilder removeFlags() {
        return removeFlags(ItemFlag.values());
    }

    public ItemBuilder setAttributes(Multimap<Attribute,AttributeModifier> map){
        return meta(meta -> meta.setAttributeModifiers(map));
    }
    public ItemBuilder addAttribute(Attribute attribute, AttributeModifier value) {
        return meta(meta -> meta.addAttributeModifier(attribute, value));
    }

    public ItemBuilder armorColor(Color color) {
        return meta(LeatherArmorMeta.class, meta -> meta.setColor(color));
    }

    public ItemStack build() {
        return this.item;
    }

    public ItemBuilder skullOwner(OfflinePlayer player) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        skullMeta.setOwningPlayer(player);
        skull.setItemMeta(skullMeta);

        return edit(item -> item.setItemMeta(skullMeta));
    }

    public ItemBuilder modelData(int modelData) {
        return meta(meta -> meta.setCustomModelData(modelData));
    }
}