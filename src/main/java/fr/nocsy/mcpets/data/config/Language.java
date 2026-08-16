package fr.nocsy.mcpets.data.config;

import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;

import fr.nocsy.mcpets.utils.Utils;

import net.kyori.adventure.text.Component;

public enum Language {

    INVENTORY_PETS_MENU("<black>☀ <dark_red>Pets <black>☀"),
    INVENTORY_PETS_MENU_INTERACTIONS("<black>☀ <dark_red>Pet <black>☀"),

    INVENTORY_MOUNTS_MENU("<black>☀ <dark_red>Mounts <black>☀"),
    INVENTORY_MOUNTS_MENU_INTERACTIONS("<black>☀ <dark_red>Mount <black>☀"),

    MOUNT_ITEM_NAME("<gold>Mount"),
    MOUNT_ITEM_DESCRIPTION("<gray>Click to mount your pet"),

    RENAME_ITEM_NAME("<gold>Rename"),
    RENAME_ITEM_DESCRIPTION("<gray>Click to rename your pet"),

    BACK_TO_PETMENU_ITEM_NAME("<red>Back to menu"),
    BACK_TO_PETMENU_ITEM_DESCRIPTION("<gray>Click to get back to the menu"),

    INVENTORY_ITEM_NAME("<gold>Inventory"),
    INVENTORY_ITEM_DESCRIPTION("<gray>Click to open the pet's inventory"),

    SKINS_ITEM_NAME("<gold>Skins"),
    SKINS_ITEM_DESCRIPTION("<gray>Click to change your pet's skin"),

    EQUIPMENT_ITEM_NAME("<gold>Equipment"),
    EQUIPMENT_DESCRIPTION("<gray>Click to open your pet's equipment"),

    NEXT_PAGE_ITEM_NAME("<gold>Next page <gray>(<yellow>%currentPage%<dark_gray>/<gray>%maxPage%)"),
    NEXT_PAGE_ITEM_DESCRIPTION("<yellow>Click<gray> to go forward"),

    PREVIOUS_PAGE_ITEM_NAME("<gold>Previous page <gray>(<yellow>%currentPage%<dark_gray>/<gray>%maxPage%)"),
    PREVIOUS_PAGE_ITEM_DESCRIPTION("<yellow>Click<gray> to go backward"),

    NICKNAME("<blue>Nickname : <gray>%nickname%"),
    NICKNAME_ITEM_LORE("<red>Click here to revoke your pet"),

    SUMMONED("<gray>A pet has been summoned !"),
    REVOKED("<gray>Your pet was revoked."),
    REVOKED_FOR_NEW_ONE("<gray>Your previous pet was revoked to summon the new one."),
    REVOKED_UNKNOWN("<red>The pet could not be spawned due to one of the following reasons :" +
            "\n<gray>- The provided <red>MythicMob in the pet config doesn't exist<gray> (try to spawn it through /mm m spawn)<gray>." +
            "\n<gray>- The world is on <red>peaceful or easy mode<gray>." +
            "\n<gray>- A region <red>prevents the mob from spawning<gray> (the anchor is an aggressive mob most likely)." +
            "\n<gray>- You have a <red>spawn protector plugin<gray>, try to spawn the mob in another world or far from spawn." +
            "\n<gray>- There exist other pets with the <red>same id<gray>. Make sure you have unique ids."),
    MYTHICMOB_NULL("<red>This pet could not be summoned. The associated mythicMob entity or file is null or was removed."),
    NO_MOB_MATCH("<red>This pet could not be summoned. The associated mythicmob isn't registered in MythicMobs."),
    NOT_ALLOWED("<red>You're not allowed to summon this pet."),
    OWNER_NOT_FOUND("<red>This pet could not be summoned. The summoner couldn't be found."),
    REVOKED_BEFORE_CHANGES("<red>Your pet was revoked before the modifications could take place."),
    NOT_MOUNTABLE("<red>This pet has no mounting point."),
    ALREADY_MOUNTING("<red>You are already riding something. Please dismount before you attempt again."),
    NOT_MOUNTABLE_HERE("<red>You can't ride a pet in this area."),
    CANT_MOUNT_PET_YET("<red>You do not have the permission to ride that pet."),
    CANT_FOLLOW_HERE("<red>Your pet can't follow you in this area."),
    TYPE_NAME_IN_CHAT("<green>Write down in the chat the name of your pet."),
    IF_WISH_TO_REMOVE_NAME("<green>If you wish to remove it, write <red>%tag%<green> in the chat."),
    NICKNAME_CHANGED_SUCCESSFULY("<green>Nickname successfully changed !"),
    NICKNAME_NOT_CHANGED("<red>Nickname could not be changed due to it being an empty string. Please try again."),
    TAG_TO_REMOVE_NAME("None"),
    ALREADY_INSIDE_VEHICULE("<gray>You're already mounting something. Please dismount your current mount to use this feature."),
    PET_DOESNT_EXIST("<red>This pet doesn't exist. Please check the id."),
    PLAYER_NOT_CONNECTED("<red>The player <gold>%player%<red> isn't connected."),
    BLACKLISTED_WORD("<red>Rename operation has been cancelled. The word %word% is not allowed in a pet name."),
    NO_ACTIVE_PET("<red>You have no active pet."),
    SPECIFY_PET("<red>You have multiple active pets. Please specify which one: <yellow>%pets%"),
    SIGNAL_STICK_GIVEN("<green>You've received an order stick. Right click to cast an order, left click to switch orders."),
    SIGNAL_STICK_SIGNAL("<gold>Active order : <yellow>%signal%"),
    LOOP_SPAWN("<red>Your pet was revoked because it seems to struggle with numerous teleportations."),
    REQUIRES_ITEM_IN_HAND("<red>You must holding an item in your hand it update it in the config."),
    ITEM_UPDATED("<green>Item updated successful with the key : <yellow>%key%"),
    ITEM_DOESNT_EXIST("<green>The item with the key <yellow>%key%<red> doesn't exist. If you want to add it you can use the <yellow>add<red> argument instead."),
    KEY_DOESNT_EXIST("<red>The specified key is not registered."),
    KEY_REMOVED("<green>The key item was removed succesfully."),
    KEY_ALREADY_EXISTS("<red>This key is already registered. Use it to replace the current item."),
    KEY_ADDED("<green>Key added successfully with the corresponding item."),
    KEY_LIST("<green>Available keys :"),

    RELOAD_SUCCESS("<green>Reloaded successfully."),
    HOW_MANY_PETS_LOADED("<green>%numberofpets% were registered successfully"),

    REQUIRES_MODELENGINE("<red>This plugin requires ModelEngine R4.0.6 or BetterModel v2.0.1. It seems that this requirement is not satisfied."),

    USAGE("<red>This command doesn't exist. \n<gray>Check out the wiki: <underlined>https://mcpets.gitbook.io/mcpets/tutorials/plugin-features/commands"),
    NO_PERM("<red>You're not allowed to use this command."),
    BLACKLISTED_WORLD("<red>MCPets is disabled in this world."),

    CATEGORY_MENU_TITLE("<black>☀ <dark_red>Pets <dark_gray>- Pick a category <black>☀"),
    CATEGORY_DOESNT_EXIST("<red>This category does not exist."),

    PET_INVENTORY_TITLE("<black>☀ <dark_red>%pet% <dark_gray>- <black>Inventory <black>☀"),

    PET_INVENTORY_COULDNOT_OPEN("<red>This inventory can not be opened as it may not exist."),

    PET_SKINS_TITLE("<black>☀ <dark_red>%pet% <dark_gray>- <black>Skins <black>☀"),

    SKIN_COULD_NOT_APPLY("<red>The skin could not be applied to the pet."),
    SKIN_APPLIED("<green>Skin changed successfully !"),

    GLOBAL_RESPAWN_TIMER_RUNNING("<red>This pet could not be spawned. You need to wait %timeLeft%s/%cooldown%s."),
    RESPAWN_TIMER_RUNNING("<red>This pet could not be spawned. It's still recovering from its wounds. You need to wait %timeLeft%s/%cooldown%s."),
    REVOKE_TIMER_RUNNING("<red>This pet could not be spawned. It's still recovering from its wounds. You need to wait %timeLeft%s/%cooldown%s."),

    PLAYER_OR_PET_DOESNT_EXIST("<red>This pet doesn't exist, or this player has never played on your server."),
    STATS_CLEARED("<green>All stats have been cleared successfully !"),
    STATS_CLEARED_FOR_PET_FOR_PLAYER("<green>All stats have been cleared successfully for the pet %petId% for the player %player%."),
    STATS_CLEARED_FOR_PET("<green>All stats have been cleared successfully for the pet %petId%"),

    PET_TAMING_PROGRESS("<gray>Taming progress <green>%progress%% <gray>- %progressbar%"),
    PET_COULD_NOT_EVOLVE("<gray>Your pet could not evolve because <red>you already own the evolution<gray>."),
    PETFOOD_DOESNT_EXIST("<red>This pet food doesn't exist."),
    PETUNLOCK_NOPERM("<red>You are not allowed to use this item to unlock the pet."),
    PETUNLOCKED("<green>You've unlocked the pet %petName%, congratulations !"),
    PETUNLOCKED_ALREADY("<red>You already own the pet <gold>%petName%<red>."),

    PET_ALREADY_TAMED("<red>This pet is already tamed."),
    PET_DOESNT_EAT("<red>This pet can not eat that food."),
    PET_FOOD_ON_COOLDOWN("<red>The pet won't eat this food for another %timeleft% seconds"),

    PET_STATUS_ALIVE("<green>Available"),
    PET_STATUS_REVOKED("<red>Unavailable <gray>(%timeleft%s left)"),
    PET_STATUS_DEAD("<red>Dead <gray>(%timeleft%s left)"),

    PET_STATS("<gold>✦ Pet's Information ✦" +
            "\n<gray>Status: %status%" +
            "\n<gold>Level <gray>- <gold>%levelname%" +
            "\n " +
            "\n<white>%health%<gray>/<white>%maxhealth% <red>❤" +
            "\n<gray>Regeneration: %regeneration% ❤/s" +
            "\n<gray>Damage Modifier: <white>%damagemodifier%%" +
            "\n<gray>Resistance Modifier: <white>%resistancemodifier%%" +
            "\n<gray>Power: <white>%power%%" +
            "\n " +
            "\n<gray>Experience: <green>%experience%/%threshold% xp" +
            "\n%progressbar%"),

    PET_STATS_EVOLUTION_ALREADY_OWNED("<red>Evolution already owned."),
    PET_STATS_MAX_LEVEL("<gray>Maximum level reached."),
    MAX_ACTIVE_PETS_REACHED("<red>You have reached the maximum number of active pets!"),
    PET_REPLACED_BY_NEW("<yellow>%oldpet% has been replaced by %newpet%!"),
    DEBUGGER_JOINING("<green>Debugger is enabled. You are now listening to it."),
    DEBUGGER_LEAVE("<green>Debugger is <gray>disabled<green>. You will not be listening to it anymore.");

    private String message;

    Language(String message) {
        this.message = message;
    }

    public void reload() {
        if (LanguageConfig.getInstance().getMap().containsKey(name().toLowerCase())) {
            message = LanguageConfig.getInstance().getMap().get(name().toLowerCase());
        }
    }

    public String getMessage() {
        String m = message;

        m = Utils.applyPlaceholders(null, m);
        return m;
    }

    public String getMessagePAPI() {
        String m = message;

        m = Utils.applyPlaceholders(null, m);
        return m;
    }

    public Component getComponent() {
        return Utils.toComponent(getMessage());
    }

    public Component getComponentWithPrefix() {
        return Utils.toComponentWithPrefix(getMessage());
    }

    public void sendMessage(Player p) {
        if (message.isEmpty()) return;
        p.sendMessage(getComponentWithPrefix());
    }

    public void sendMessage(CommandSender sender) {
        if (message.isEmpty()) return;
        sender.sendMessage(getComponentWithPrefix());
    }

    public void sendMessageFormatted(CommandSender sender, FormatArg... args) {
        if (message.isEmpty()) return;

        sender.sendMessage(Utils.toComponentWithPrefix(getMessageFormatted(args)));
    }

    public String getMessageFormatted(FormatArg... args) {
        String toSend = getMessage();
        for (FormatArg arg : args) {
            toSend = arg.applyToString(toSend);
        }

        return toSend;
    }

    public Component getComponentFormatted(FormatArg... args) {
        return Utils.toComponent(getMessageFormatted(args));
    }

}
