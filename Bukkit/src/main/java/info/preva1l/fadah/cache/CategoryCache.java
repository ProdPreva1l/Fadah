package info.preva1l.fadah.cache;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.hooks.impl.EcoItemsHook;
import info.preva1l.fadah.records.Category;
import info.preva1l.fadah.utils.SetHelper;
import info.preva1l.fadah.utils.config.BasicConfig;
import lombok.experimental.UtilityClass;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@UtilityClass
public final class CategoryCache {
    private List<Category> categories = new ArrayList<>();
    private final BasicConfig categoriesFile = Fadah.getINSTANCE().getCategoriesFile();

    public void update() {
        categories = fillListWithCategories();
    }

    public Category getCategory(String id) {
        return categories.stream().filter(category -> category.id().equals(id)).findFirst().orElse(null);
    }

    public List<Category> getCategories() {
        return Collections.unmodifiableList(categories);
    }

    @Nullable
    public String getCategoryForItem(ItemStack itemStack) {
        for (Category category : getCategories()) {
            if (category.isCustomItems()) {
                if (Config.HOOK_ECO_ITEMS.toBoolean()
                        && Fadah.getINSTANCE().getHookManager().getHook(EcoItemsHook.class).isPresent()) {
                    EcoItemsHook ecoItemsHook = (EcoItemsHook) Fadah.getINSTANCE().getHookManager().getHook(EcoItemsHook.class).get();
                    if (ecoItemsHook.isEcoItem(itemStack)) return category.id();
                }
            }
            if (category.materials() != null && category.materials().contains(itemStack.getType()))
                return category.id();
        }
        return null;
    }

    public List<Category> fillListWithCategories() {
        List<Category> list = new ArrayList<>();
        for (String key : categoriesFile.getConfiguration().getKeys(false)) {
            String name = categoriesFile.getString(key + ".name");
            Material icon = Material.getMaterial(categoriesFile.getString(key + ".icon"));
            int priority = categoriesFile.getInt(key + ".priority");
            int modelData = categoriesFile.getInt(key + ".icon-model-data");

            List<String> description = categoriesFile.getStringList(key + ".description");
            List<String> materialsList = categoriesFile.getStringList(key + ".materials");
            Set<Material> materials = null;
            if (!materialsList.isEmpty()) {
                if (!materialsList.get(0).equals(key + ".materials"))
                    materials = SetHelper.stringSetToMaterialSet(SetHelper.listToSet(materialsList));
            }

            boolean isCustomItems = categoriesFile.getBoolean(key + ".custom-items");
            Category.CustomItemMode customItemMode = Category.CustomItemMode.API;
            String cim = categoriesFile.getString(key + ".custom-item-mode").toUpperCase();
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
                customItemIDs = categoriesFile.getStringList(key + ".custom-item-ids");
            list.add(new Category(key, name, priority, modelData, (icon == null ? Material.GRASS_BLOCK : icon), description, materials, isCustomItems, customItemMode, SetHelper.listToSet(customItemIDs)));
        }
        list.sort(Comparator.comparingInt(Category::priority).reversed());
        return list;
    }
}