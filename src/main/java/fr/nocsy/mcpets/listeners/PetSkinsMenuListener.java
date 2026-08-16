package fr.nocsy.mcpets.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.entity.Player;

import fr.nocsy.mcpets.data.PetSkin;

/**
 * Clears skin menu metadata on close.
 */
public class PetSkinsMenuListener implements Listener {

    @EventHandler
    public void close(final InventoryCloseEvent e) {
        if (e.getPlayer() instanceof Player p) {
            if (p.hasMetadata("MCPetsSkins")) {
                PetSkin.removeMetadata(p);
            }
        }
    }
}
