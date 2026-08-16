package fr.nocsy.mcpets.data.inventories;

import lombok.Getter;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.nocsy.mcpets.data.Category;
import fr.nocsy.mcpets.data.CategoryType;
import fr.nocsy.mcpets.data.config.Language;
import fr.nocsy.mcpets.data.menus.MenuContext;
import fr.nocsy.mcpets.data.menus.MenuService;
import fr.nocsy.mcpets.utils.PDCTag;

public class CategoriesMenu {

    @Getter
    private static final String title = Language.CATEGORY_MENU_TITLE.getMessage();

    public static void open(final Player p) {
        openFiltered(p, CategoryType.PET);
    }

    public static void openFiltered(final Player p, final CategoryType filterType) {
        openFiltered(p, filterType, 0);
    }

    public static void openFiltered(final Player p, final CategoryType filterType, final int page) {
        final String filter = filterType != null ? filterType.name() : "PET";
        MenuService.getInstance().openCategories(p, filter);
    }

    public static void openSubCategory(final Player p, final ItemStack it) {
        if (it == null || !it.hasItemMeta()) {
            return;
        }
        final String tag = PDCTag.get(it.getItemMeta());
        if (tag == null || !tag.startsWith("MCPetsCategory;")) {
            return;
        }
        final String id = tag.replace("MCPetsCategory;", "");
        final Category category = Category.getFromId(id);
        if (category == null) {
            return;
        }
        final MenuContext ctx = MenuContext.of(p).withCategory(category).withPage(1)
                .withFilter(category.getCategoryType() != null ? category.getCategoryType().name() : "PET");
        MenuService.getInstance().open("category", ctx);
    }

}
