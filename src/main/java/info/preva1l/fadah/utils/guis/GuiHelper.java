package info.preva1l.fadah.utils.guis;

import info.preva1l.fadah.config.Menus;
import lombok.experimental.UtilityClass;
import org.bukkit.inventory.ItemStack;


@UtilityClass
public class GuiHelper {
    public ItemStack constructButton(GuiButtonType type) throws GuiButtonException {
        return switch (type) {
            case BACK -> new ItemBuilder(Menus.BACK_BUTTON_ICON.toMaterial())
                    .name(Menus.BACK_BUTTON_NAME.toFormattedString())
                    .modelData(Menus.BACK_BUTTON_MODEL_DATA.toInteger())
                    .lore(Menus.BACK_BUTTON_LORE.toLore()).build();
            case CLOSE -> new ItemBuilder(Menus.CLOSE_BUTTON_ICON.toMaterial())
                    .name(Menus.CLOSE_BUTTON_NAME.toFormattedString())
                    .modelData(Menus.CLOSE_BUTTON_MODEL_DATA.toInteger())
                    .lore(Menus.CLOSE_BUTTON_LORE.toLore()).build();
            case NEXT_PAGE -> new ItemBuilder(Menus.NEXT_BUTTON_ICON.toMaterial())
                    .name(Menus.NEXT_BUTTON_NAME.toFormattedString())
                    .modelData(Menus.NEXT_BUTTON_MODEL_DATA.toInteger())
                    .lore(Menus.NEXT_BUTTON_LORE.toLore()).build();
            case PREVIOUS_PAGE -> new ItemBuilder(Menus.PREVIOUS_BUTTON_ICON.toMaterial())
                    .name(Menus.PREVIOUS_BUTTON_NAME.toFormattedString())
                    .modelData(Menus.PREVIOUS_BUTTON_MODEL_DATA.toInteger())
                    .lore(Menus.PREVIOUS_BUTTON_LORE.toLore()).build();
            case SCROLL_NEXT -> new ItemBuilder(Menus.SCROLL_NEXT_BUTTON_ICON.toMaterial())
                    .name(Menus.SCROLL_NEXT_BUTTON_NAME.toFormattedString())
                    .modelData(Menus.SCROLL_NEXT_BUTTON_MODEL_DATA.toInteger())
                    .lore(Menus.SCROLL_NEXT_BUTTON_LORE.toLore()).build();
            case SCROLL_PREVIOUS -> new ItemBuilder(Menus.SCROLL_PREVIOUS_BUTTON_ICON.toMaterial())
                    .name(Menus.SCROLL_PREVIOUS_BUTTON_NAME.toFormattedString())
                    .modelData(Menus.SCROLL_PREVIOUS_BUTTON_MODEL_DATA.toInteger())
                    .lore(Menus.SCROLL_PREVIOUS_BUTTON_LORE.toLore()).build();
            case CANCEL -> new ItemBuilder(Menus.CANCEL_BUTTON_ICON.toMaterial())
                    .name(Menus.CANCEL_BUTTON_NAME.toFormattedString())
                    .modelData(Menus.CANCEL_BUTTON_MODEL_DATA.toInteger())
                    .lore(Menus.CANCEL_BUTTON_LORE.toLore()).build();
            case CONFIRM -> new ItemBuilder(Menus.CONFIRM_BUTTON_ICON.toMaterial())
                    .name(Menus.CONFIRM_BUTTON_NAME.toFormattedString())
                    .modelData(Menus.CONFIRM_BUTTON_MODEL_DATA.toInteger())
                    .lore(Menus.CONFIRM_BUTTON_LORE.toLore()).build();
            case BORDER -> new ItemBuilder(Menus.BORDER_ICON.toMaterial())
                    .name(Menus.BORDER_NAME.toFormattedString())
                    .modelData(Menus.BORDER_MODEL_DATA.toInteger())
                    .lore(Menus.BORDER_LORE.toLore()).build();
            case GENERIC ->
                    throw new GuiButtonException("Attempted to create a button with type \"GENERIC\" without required params.");
        };
    }
}
