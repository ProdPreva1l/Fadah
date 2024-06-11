package info.preva1l.fadah.utils.guis;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.utils.BasicConfig;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LayoutManager {
    private final List<GuiLayout> guiLayouts = new ArrayList<>();

    public void loadLayout(BasicConfig config) {
        final MenuType menuType = switch (config.getFileName()) {
            case "main.yml": yield MenuType.MAIN;
            case "new-listing.yml": yield MenuType.NEW_LISTING;
            case "profile.yml": yield MenuType.PROFILE;
            case "confirm.yml": yield MenuType.CONFIRM_PURCHASE;
            case "collection-box.yml": yield MenuType.COLLECTION_BOX;
            case "expired-listings.yml": yield MenuType.EXPIRED_LISTINGS;
            case "historic-items.yml": yield MenuType.HISTORY;
            case "active-listings.yml": yield MenuType.ACTIVE_LISTINGS;
            default: throw new IllegalStateException("The config file %s is not related to a GuiLayout".formatted(config.getFileName()));
        };

        final List<Integer> fillerSlots = new ArrayList<>();
        final List<Integer> paginationSlots = new ArrayList<>();
        final List<Integer> scrollbarSlots = new ArrayList<>();
        final HashMap<Integer, ButtonType> buttonSlots = new HashMap<>();

        ConfigurationSection layoutSection = config.getConfiguration().getConfigurationSection("layout");
        if (layoutSection == null) {
            Fadah.getConsole().severe("Gui Layout for the GUI %s is invalid! Missing the Layout config section.");
            return;
        }

        for (String key : layoutSection.getKeys(false)) {
            int slotNumber = Integer.parseInt(key);
            ButtonType buttonType;
            String temp = layoutSection.getString(key);
            if (temp == null || temp.isBlank()) {
                Fadah.getConsole().severe("Gui Layout for the GUI %s is invalid! Slot: %s is an empty string?".formatted(menuType.toString(), slotNumber));
                return;
            }

            try {
                buttonType = ButtonType.valueOf(temp);
            } catch (IllegalArgumentException e) {
                Fadah.getConsole().severe("Gui Layout for the GUI %s is invalid! Slot: %s Button Type %s does not exist!".formatted(menuType.toString(), slotNumber, temp));
                return;
            }

            if (buttonType.equals(ButtonType.FILLER)) {
                fillerSlots.add(slotNumber);
                continue;
            }
            if (buttonType.equals(ButtonType.PAGINATION_ITEM)) {
                paginationSlots.add(slotNumber);
                continue;
            }
            if (buttonType.equals(ButtonType.SCROLLBAR_ITEM)) {
                scrollbarSlots.add(slotNumber);
                continue;
            }
            buttonSlots.put(slotNumber, buttonType);
        }

        guiLayouts.add(new GuiLayout(menuType, fillerSlots, paginationSlots, scrollbarSlots, buttonSlots, config));
    }

    public @NotNull GuiLayout getLayout(FastInv inventory) {
        for (GuiLayout layout : guiLayouts) {
            if (inventory.getMenuType() == layout.menuType()) return layout;
        }
        throw new IllegalStateException("No GuiLayout found for inventory type %s".formatted(inventory.getMenuType()));
    }

    public enum MenuType {
        MAIN,
        NEW_LISTING,
        PROFILE,
        HISTORY,
        EXPIRED_LISTINGS,
        CONFIRM_PURCHASE,
        COLLECTION_BOX,
        ACTIVE_LISTINGS,
        /**
         * Guis without layouts
         */
        SHULKER_PREVIEW,
        LAYOUT_EDITOR
    }

    public enum ButtonType {
        /**
         * Pagination Buttons
         */
        PAGINATION_CONTROL_ONE,
        PAGINATION_CONTROL_TWO,
        PAGINATION_ITEM,
        /**
         * Scrollbar buttons
         */
        SCROLLBAR_CONTROL_ONE,
        SCROLLBAR_CONTROL_TWO,
        SCROLLBAR_ITEM,
        /**
         * Main Menu Specific Items
         */
        PROFILE,
        FILTER,
        FILTER_DIRECTION,
        /**
         * Confirm Menu Specific Items
         */
        CONFIRM,
        CANCEL,
        /**
         * Misc Items
         */
        NO_ITEMS,
        FILLER,
        BACK,
        SEARCH
    }
}
