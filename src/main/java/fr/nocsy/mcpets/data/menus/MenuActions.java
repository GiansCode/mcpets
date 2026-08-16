package fr.nocsy.mcpets.data.menus;

import fr.nocsy.mcpets.data.Category;
import fr.nocsy.mcpets.data.Pet;
import fr.nocsy.mcpets.data.PetSkin;
import fr.nocsy.mcpets.data.config.GlobalConfig;
import fr.nocsy.mcpets.data.config.Language;
import fr.nocsy.mcpets.data.inventories.PetInventory;
import fr.nocsy.mcpets.listeners.PetInteractionMenuListener;
import fr.nocsy.mcpets.utils.PDCTag;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Executes YAML-defined menu actions.
 */
public final class MenuActions {

    private MenuActions() {
    }

    public static void execute(final String actionRaw, final MenuContext context, final ItemStack clicked) {
        if (actionRaw == null || actionRaw.isBlank() || "NONE".equalsIgnoreCase(actionRaw)) {
            return;
        }
        final Player player = context.getPlayer();
        if (player == null) {
            return;
        }

        String action = actionRaw.trim();
        String arg = null;
        final int colon = action.indexOf(':');
        if (colon > 0) {
            arg = action.substring(colon + 1).trim();
            action = action.substring(0, colon).trim();
        }
        action = action.toUpperCase();

        switch (action) {
            case "CLOSE" -> player.closeInventory();
            case "BACK" -> MenuService.getInstance().openBack(context);
            case "PREVIOUS_PAGE" -> {
                context.setPage(Math.max(1, context.getPage() - 1));
                MenuService.getInstance().reopen(context);
            }
            case "NEXT_PAGE" -> {
                context.setPage(context.getPage() + 1);
                MenuService.getInstance().reopen(context);
            }
            case "OPEN_MENU" -> {
                if (arg != null) {
                    MenuService.getInstance().open(arg, context);
                }
            }
            case "BACK_TO_PET_MENU", "OPEN_PETS" -> MenuService.getInstance().openPets(player);
            case "BACK_TO_MOUNT_MENU", "OPEN_MOUNTS" -> MenuService.getInstance().openMounts(player);
            case "BACK_TO_CATEGORIES" -> {
                final String filter = context.getFilter() != null ? context.getFilter()
                        : (context.getCategory() != null && context.getCategory().getCategoryType() != null
                        ? context.getCategory().getCategoryType().name() : "PET");
                MenuService.getInstance().openCategories(player, filter);
            }
            case "OPEN_CATEGORY" -> {
                String catId = arg;
                if (catId == null && clicked != null) {
                    catId = extractTag(clicked, "MCPetsCategory;");
                }
                if (catId != null) {
                    final Category category = Category.getFromId(catId);
                    if (category != null) {
                        context.withCategory(category).withPage(1);
                        MenuService.getInstance().open("category", context);
                    }
                }
            }
            case "SPAWN_PET", "SPAWN_MOUNT" -> {
                String petId = arg;
                if (petId == null && clicked != null) {
                    petId = extractPetId(clicked);
                }
                if (petId != null) {
                    final Pet template = Pet.getFromId(petId);
                    if (template != null) {
                        final Pet pet = template.copy();
                        pet.spawnWithMessage(player);
                        player.closeInventory();
                    }
                }
            }
            case "REVOKE" -> {
                if (context.getPet() != null) {
                    PetInteractionMenuListener.revoke(player, context.getPet());
                    player.closeInventory();
                }
            }
            case "MOUNT" -> {
                if (context.getPet() != null) {
                    PetInteractionMenuListener.mount(player, context.getPet());
                    player.closeInventory();
                }
            }
            case "RENAME" -> {
                player.closeInventory();
                PetInteractionMenuListener.changeName(player);
            }
            case "OPEN_SKINS", "SKINS" -> {
                if (context.getPet() != null) {
                    MenuService.getInstance().openSkins(player, context.getPet());
                }
            }
            case "OPEN_INVENTORY", "PET_INVENTORY" -> {
                if (context.getPet() != null) {
                    final PetInventory inv = PetInventory.get(context.getPet());
                    if (inv != null) {
                        inv.open(player);
                    }
                }
            }
            case "GIVE_SIGNAL_STICK" -> {
                if (context.getPet() != null && context.getPet().getSignalStick() != null) {
                    player.getInventory().addItem(context.getPet().getSignalStick().clone());
                    player.closeInventory();
                }
            }
            case "APPLY_SKIN" -> {
                if (context.getPet() != null && clicked != null) {
                    final PetSkin skin = PetSkin.fromIcon(clicked);
                    if (skin != null) {
                        if (skin.apply(context.getPet())) {
                            Language.SKIN_APPLIED.sendMessage(player);
                        } else {
                            Language.SKIN_COULD_NOT_APPLY.sendMessage(player);
                        }
                        player.closeInventory();
                    }
                }
            }
            case "EDITOR_NAVIGATE" -> {
                if (arg != null) {
                    MenuService.getInstance().openEditor(player, arg);
                }
            }
            default -> {
                // Unknown action — ignore
            }
        }
    }

    public static boolean matchesCondition(final String condition, final MenuContext context) {
        if (condition == null || condition.isBlank()) {
            return true;
        }
        final String c = condition.toUpperCase();
        final Pet pet = context.getPet();
        final GlobalConfig g = GlobalConfig.getInstance();
        return switch (c) {
            case "ALWAYS", "TRUE" -> true;
            case "ACTIVATE_BACK_MENU_ICON" -> g.isActivateBackMenuIcon();
            case "ENABLE_CLICK_BACK_TO_MENU" -> g.isEnableClickBackToMenu();
            case "NAMEABLE" -> g.isNameable();
            case "HAS_SKINS" -> pet != null && pet.hasSkins();
            case "MOUNTABLE" -> g.isMountable() && pet != null && pet.isMountable();
            case "HAS_PET_INVENTORY" -> pet != null && pet.getInventorySize() > 0;
            case "HAS_SIGNALS" -> pet != null && !pet.getSignals().isEmpty() && pet.isEnableSignalStickFromMenu();
            case "TAMING_COMPLETE" -> pet != null && pet.getTamingProgress() >= 1;
            default -> true;
        };
    }

    private static String extractTag(final ItemStack item, final String prefix) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }
        final String tag = PDCTag.get(item.getItemMeta());
        if (tag != null && tag.startsWith(prefix)) {
            return tag.substring(prefix.length());
        }
        return null;
    }

    private static String extractPetId(final ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }
        final ItemMeta meta = item.getItemMeta();
        final String tag = PDCTag.get(meta);
        if (tag == null) {
            return null;
        }
        // Pet icons use various tags; try common patterns
        if (tag.contains(";")) {
            final String[] parts = tag.split(";");
            if (parts.length >= 2) {
                return parts[parts.length - 1];
            }
        }
        final Pet fromItem = Pet.getFromIcon(item);
        return fromItem != null ? fromItem.getId() : null;
    }
}
