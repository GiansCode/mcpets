package fr.nocsy.mcpets.data.inventories;

import java.util.UUID;

import lombok.Getter;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import fr.nocsy.mcpets.data.config.Language;
import fr.nocsy.mcpets.data.menus.MenuService;

/**
 * Menu to display the list of available mounts for a player.
 * Layout is defined in menus/mounts.yml.
 */
public class MountMenu {

    @Getter
    private static final String title = Language.INVENTORY_MOUNTS_MENU.getMessage();

    @Getter
    private final Inventory inventory = null;

    @Getter
    private final UUID owner;

    @Getter
    private final int page;

    public MountMenu(final Player p, final int page) {
        owner = p.getUniqueId();
        this.page = page;
    }

    public void open(final Player p) {
        MenuService.getInstance().openMounts(p);
    }

}
