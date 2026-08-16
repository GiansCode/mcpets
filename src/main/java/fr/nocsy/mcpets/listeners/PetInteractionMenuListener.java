package fr.nocsy.mcpets.listeners;

import java.util.UUID;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import lombok.Getter;

import org.jetbrains.annotations.NotNull;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import fr.nocsy.mcpets.PPermission;
import fr.nocsy.mcpets.utils.Utils;
import fr.nocsy.mcpets.data.Pet;
import fr.nocsy.mcpets.data.PetSkin;
import fr.nocsy.mcpets.data.config.Language;
import fr.nocsy.mcpets.data.PetDespawnReason;
import fr.nocsy.mcpets.data.config.FormatArg;
import fr.nocsy.mcpets.data.inventories.PetInventory;
import fr.nocsy.mcpets.data.menus.MenuService;
import fr.nocsy.mcpets.utils.FoliaCompat;

/**
 * Keeps rename chat handling and static helpers used by menu actions.
 * Inventory clicks for interaction menus are handled by Triumph GUI.
 */
public class PetInteractionMenuListener implements Listener {

    @Getter
    private static final List<UUID> waitingForAnswer = new CopyOnWriteArrayList<>();

    public static void changeName(@NotNull final Player p) {
        if (!waitingForAnswer.contains(p.getUniqueId())) {
            waitingForAnswer.add(p.getUniqueId());
        }
        Language.TYPE_NAME_IN_CHAT.sendMessage(p);
        Language.IF_WISH_TO_REMOVE_NAME.sendMessageFormatted(p, new FormatArg("%tag%", Language.TAG_TO_REMOVE_NAME.getMessage()));
    }

    public static void mount(@NotNull final Player p, final Pet pet) {
        if (p.isInsideVehicle()) {
            Language.ALREADY_INSIDE_VEHICULE.sendMessage(p);
        } else if (!pet.setMount(p)) {
            Language.NOT_MOUNTABLE.sendMessage(p);
        }
    }

    public static void inventory(final Player p, final Pet pet) {
        final PetInventory inventory = PetInventory.get(pet);
        if (inventory != null) inventory.open(p);
    }

    public static void skins(final Player p, final Pet pet) {
        FoliaCompat.runEntityLater(p, () -> PetSkin.openInventory(p, pet), 2L);
    }

    public static void revoke(final Player p, @NotNull final Pet pet) {
        pet.despawn(PetDespawnReason.REVOKE);
        Language.REVOKED.sendMessage(p);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void chat(final AsyncPlayerChatEvent e) {
        final Player p = e.getPlayer();
        if (!waitingForAnswer.contains(p.getUniqueId())) {
            return;
        }
        e.setCancelled(true);
        waitingForAnswer.remove(p.getUniqueId());

        final Pet pet = Pet.getFromLastInteractedWith(p);
        if (pet == null || !pet.isStillHere()) {
            Language.REVOKED_BEFORE_CHANGES.sendMessage(p);
            return;
        }

        String name = e.getMessage();
        if (!p.hasPermission(PPermission.COLOR.getPermission())) {
            name = Utils.stripColors(name);
        }
        pet.setDisplayName(name, true);
        Language.NICKNAME_CHANGED_SUCCESSFULY.sendMessage(p);
        FoliaCompat.runEntityLater(p, () -> MenuService.getInstance().openInteraction(p, pet, false), 1L);
    }
}
