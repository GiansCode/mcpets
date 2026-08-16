package fr.nocsy.mcpets.data.inventories;

import java.util.UUID;

import lombok.Getter;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import fr.nocsy.mcpets.data.config.Language;
import fr.nocsy.mcpets.data.menus.MenuService;

/**
 * Entry point for the pets list menu. Layout is defined in menus/pets.yml.
 */
public class PetMenu {

    @Getter
    private static final String title = Language.INVENTORY_PETS_MENU.getMessage();

    @Getter
    private final Inventory inventory = null;

    @Getter
    private final UUID owner;

    public PetMenu(final Player p, final int page) {
        owner = p.getUniqueId();
    }

    public void open(final Player p) {
        MenuService.getInstance().openPets(p);
    }

}
