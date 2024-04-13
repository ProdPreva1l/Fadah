package info.preva1l.fadah.utils.guis;

import info.preva1l.fadah.config.Menus;
import lombok.experimental.UtilityClass;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;


@UtilityClass
public class GuiHelper {
    public ItemStack constructButton(GuiButtonType type) throws GuiButtonException {
        return switch (type) {
            case BACK -> new ItemBuilder(Material.FEATHER)
                    .name(Menus.BACK_BUTTON_NAME.toFormattedString())
                    .lore(Menus.BACK_BUTTON_LORE.toLore()).build();
            case CLOSE -> new ItemBuilder(Material.BARRIER)
                    .name(Menus.CLOSE_BUTTON_NAME.toFormattedString())
                    .lore(Menus.CLOSE_BUTTON_LORE.toLore()).build();
            case NEXT_PAGE -> new ItemBuilder(Material.ARROW)
                    .name(Menus.NEXT_BUTTON_NAME.toFormattedString())
                    .lore(Menus.NEXT_BUTTON_LORE.toLore()).build();
            case PREVIOUS_PAGE -> new ItemBuilder(Material.ARROW)
                    .name(Menus.PREVIOUS_BUTTON_NAME.toFormattedString())
                    .lore(Menus.PREVIOUS_BUTTON_LORE.toLore()).build();
            case SCROLL_NEXT -> new ItemBuilder(Material.ARROW)
                    .name(Menus.SCROLL_NEXT_BUTTON_NAME.toFormattedString())
                    .lore(Menus.SCROLL_NEXT_BUTTON_LORE.toLore()).build();
            case SCROLL_PREVIOUS -> new ItemBuilder(Material.ARROW)
                    .name(Menus.SCROLL_PREVIOUS_BUTTON_NAME.toFormattedString())
                    .lore(Menus.SCROLL_PREVIOUS_BUTTON_LORE.toLore()).build();
            case CANCEL -> new ItemBuilder(Material.RED_CONCRETE)
                    .name(Menus.CANCEL_BUTTON_NAME.toFormattedString())
                    .lore(Menus.CANCEL_BUTTON_LORE.toLore()).build();
            case CONFIRM -> new ItemBuilder(Material.LIME_CONCRETE)
                    .name(Menus.CONFIRM_BUTTON_NAME.toFormattedString())
                    .lore(Menus.CONFIRM_BUTTON_LORE.toLore()).build();
            case GENERIC ->
                    throw new GuiButtonException("Attempted to create a button with type \"GENERIC\" without required params.");
            default -> throw new GuiButtonException("Invalid Button Type.");
        };
    }
    public ItemStack constructButton(GuiButtonType type, Material material, String name, List<String> lore) throws GuiButtonException {
        return switch (type) {
            case BACK -> new ItemBuilder(Material.FEATHER)
                    .name(Menus.BACK_BUTTON_NAME.toFormattedString())
                    .lore(Menus.BACK_BUTTON_LORE.toLore()).build();
            case CLOSE -> new ItemBuilder(Material.BARRIER)
                    .name(Menus.CLOSE_BUTTON_NAME.toFormattedString())
                    .lore(Menus.CLOSE_BUTTON_LORE.toLore()).build();
            case NEXT_PAGE -> new ItemBuilder(Material.FEATHER)
                    .name(Menus.NEXT_BUTTON_NAME.toFormattedString())
                    .lore(Menus.NEXT_BUTTON_LORE.toLore()).build();
            case PREVIOUS_PAGE -> new ItemBuilder(Material.FEATHER)
                    .name(Menus.PREVIOUS_BUTTON_NAME.toFormattedString())
                    .lore(Menus.PREVIOUS_BUTTON_LORE.toLore()).build();
            case SCROLL_NEXT -> new ItemBuilder(Material.ARROW)
                    .name(Menus.SCROLL_NEXT_BUTTON_NAME.toFormattedString())
                    .lore(Menus.SCROLL_NEXT_BUTTON_LORE.toLore()).build();
            case SCROLL_PREVIOUS -> new ItemBuilder(Material.ARROW)
                    .name(Menus.SCROLL_PREVIOUS_BUTTON_NAME.toFormattedString())
                    .lore(Menus.SCROLL_PREVIOUS_BUTTON_LORE.toLore()).build();
            case GENERIC -> new ItemBuilder(material)
                    .name(name)
                    .lore(lore).build();
            default -> throw new GuiButtonException("Invalid Button Type.");
        };
    }
}
