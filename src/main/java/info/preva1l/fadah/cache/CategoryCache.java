package info.preva1l.fadah.cache;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.hooks.HookManager;
import info.preva1l.fadah.hooks.impl.EcoItemsHook;
import info.preva1l.fadah.records.Category;
import info.preva1l.fadah.utils.SetHelper;
import lombok.experimental.UtilityClass;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@UtilityClass
public final class CategoryCache {
    private final List<Category> categories = new ArrayList<>();

    public void purgeCategories() {
        categories.clear();
    }

    public Category getCategory(String id) {
        return categories.stream().filter(category -> category.id().equals(id)).findFirst().orElse(null);
    }

    public List<Category> getCategories() {
        return new ArrayList<>(categories);
    }

    @Nullable
    public String getCategoryForItem(ItemStack itemStack) {
        List<Category> ctgs = getCategories();
        ctgs.sort(Comparator.comparingInt(Category::priority).reversed());
        for (Category category : ctgs) {
            if (category.isCustomItems()) {
                if (Config.HOOK_ECO_ITEMS.toBoolean() && HookManager.getHook(EcoItemsHook.class).isPresent()) {
                    EcoItemsHook ecoItemsHook = (EcoItemsHook) HookManager.getHook(EcoItemsHook.class).get();
                    if (ecoItemsHook.isEcoItem(itemStack)) return category.id();
                }
            }
            if (category.materials() != null && category.materials().contains(itemStack.getType()))
                return category.id();
        }
        return null;
    }

    public void loadCategories() {
        for (String key : Fadah.getINSTANCE().getCategoriesFile().getConfiguration().getKeys(false)) {
            String name = Fadah.getINSTANCE().getCategoriesFile().getString(key + ".name");
            Material icon = Material.getMaterial(Fadah.getINSTANCE().getCategoriesFile().getString(key + ".icon"));
            int priority = Fadah.getINSTANCE().getCategoriesFile().getInt(key + ".priority");
            int modelData = Fadah.getINSTANCE().getCategoriesFile().getInt(key + ".icon_model_data");

            List<String> description = Fadah.getINSTANCE().getCategoriesFile().getStringList(key + ".description");
            List<String> materialsList = Fadah.getINSTANCE().getCategoriesFile().getStringList(key + ".materials");
            Set<Material> materials = null;
            if (!materialsList.isEmpty()) {
                if (!materialsList.get(0).equals(key + ".materials"))
                    materials = SetHelper.stringSetToMaterialSet(SetHelper.listToSet(materialsList));
            }

            boolean isCustomItems = Fadah.getINSTANCE().getCategoriesFile().getBoolean(key + ".custom-items");
            Category.CustomItemMode customItemMode = Category.CustomItemMode.API;
            String cim = Fadah.getINSTANCE().getCategoriesFile().getString(key + ".custom-item-mode").toUpperCase();
            try {
                customItemMode = Category.CustomItemMode.valueOf(cim);
            } catch (EnumConstantNotPresentException | IllegalArgumentException ignored) {
                Fadah.getConsole().severe("-----------------------------");
                Fadah.getConsole().severe("Config Incorrect!");
                Fadah.getConsole().severe("Custom Item Mode: " + cim);
                Fadah.getConsole().severe("Does Not Exist!");
                Fadah.getConsole().severe("Defaulting to API");
                Fadah.getConsole().severe("-----------------------------");
            }
            List<String> customItemIDs = null;
            if (isCustomItems)
                customItemIDs = Fadah.getINSTANCE().getCategoriesFile().getStringList(key + ".custom-item-ids");
            categories.add(new Category(key, name, priority, modelData, (icon == null ? Material.GRASS_BLOCK : icon), description, materials, isCustomItems, customItemMode, SetHelper.listToSet(customItemIDs)));
        }
    }
}