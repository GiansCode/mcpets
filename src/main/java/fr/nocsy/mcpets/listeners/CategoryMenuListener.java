package fr.nocsy.mcpets.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import fr.nocsy.mcpets.data.Category;
import fr.nocsy.mcpets.data.CategoryType;
import fr.nocsy.mcpets.data.config.GlobalConfig;
import fr.nocsy.mcpets.data.inventories.PetInventoryHolder;
import fr.nocsy.mcpets.data.menus.MenuService;

/**
 * Handles outside-click back navigation for category menus when still using holders.
 * Primary navigation is Triumph YAML BACK actions.
 */
public class CategoryMenuListener implements Listener {

    @EventHandler
    public void click(final InventoryClickEvent e) {
        if (!(e.getInventory().getHolder() instanceof final PetInventoryHolder holder)) {
            return;
        }
        if (holder.getType() != PetInventoryHolder.Type.CATEGORY_MENU) {
            return;
        }
        if (!(e.getWhoClicked() instanceof final Player p)) {
            return;
        }
        e.setCancelled(true);
        if (e.getClickedInventory() == null && GlobalConfig.getInstance().isEnableClickBackToMenu()) {
            final Category viewed = Category.getCategoryView(p);
            final String filter = viewed != null && viewed.getCategoryType() == CategoryType.MOUNT ? "MOUNT" : "PET";
            Category.unregisterPlayerView(p);
            MenuService.getInstance().openCategories(p, filter);
        }
    }
}
