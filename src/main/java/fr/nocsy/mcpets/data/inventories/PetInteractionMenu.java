package fr.nocsy.mcpets.data.inventories;

import java.util.UUID;

import lombok.Getter;

import org.jetbrains.annotations.NotNull;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import fr.nocsy.mcpets.data.Pet;
import fr.nocsy.mcpets.data.config.Language;
import fr.nocsy.mcpets.data.menus.MenuService;

public class PetInteractionMenu {

    @Getter
    private static final String title = Language.INVENTORY_PETS_MENU_INTERACTIONS.getMessage();

    @Getter
    private final Inventory inventory = null;

    private final Pet pet;
    private final UUID owner;

    public PetInteractionMenu(@NotNull final Pet pet, final UUID owner) {
        this.pet = pet;
        this.owner = owner;
    }

    public void open(final Player p) {
        MenuService.getInstance().openInteraction(p, pet, false);
    }

}
