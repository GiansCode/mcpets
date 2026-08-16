package fr.nocsy.mcpets.data.menus;

import fr.nocsy.mcpets.MCPets;
import fr.nocsy.mcpets.data.Category;
import fr.nocsy.mcpets.data.Pet;
import fr.nocsy.mcpets.data.config.Language;
import fr.nocsy.mcpets.utils.Utils;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Resolves user-friendly %placeholder% tokens before MiniMessage parsing.
 */
public final class MenuPlaceholders {

    private static final Pattern PLACEHOLDER = Pattern.compile("%([a-zA-Z0-9_]+)%");

    private MenuPlaceholders() {
    }

    public static String apply(String input, final MenuContext context) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        final Map<String, String> values = buildValues(context);
        final Matcher matcher = PLACEHOLDER.matcher(input);
        final StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            final String key = matcher.group(1).toLowerCase(Locale.ROOT);
            String replacement = values.getOrDefault(key, matcher.group(0));
            // Language passthrough: %language_KEY%
            if (key.startsWith("language_")) {
                final String langKey = key.substring("language_".length()).toUpperCase(Locale.ROOT);
                try {
                    replacement = Language.valueOf(langKey).getMessage();
                } catch (IllegalArgumentException ignored) {
                    replacement = matcher.group(0);
                }
            }
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement == null ? "" : replacement));
        }
        matcher.appendTail(sb);
        String result = sb.toString();

        final Player player = context != null ? context.getPlayer() : null;
        if (player != null && MCPets.getPlaceholderAPI() != null && Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            try {
                result = PlaceholderAPI.setPlaceholders(player, result);
            } catch (final Exception ignored) {
            }
        }
        return result;
    }

    public static List<String> apply(final List<String> lines, final MenuContext context) {
        if (lines == null) {
            return List.of();
        }
        return lines.stream().map(line -> apply(line, context)).collect(Collectors.toList());
    }

    private static Map<String, String> buildValues(final MenuContext context) {
        final Map<String, String> map = new HashMap<>();
        if (context == null) {
            return map;
        }
        final Player player = context.getPlayer();
        if (player != null) {
            map.put("player", player.getName());
            map.put("player_uuid", player.getUniqueId().toString());
            map.put("player_name", player.getName());
        }
        map.put("page", String.valueOf(context.getPage()));
        map.put("menu_id", context.getCurrentMenuId() != null ? context.getCurrentMenuId() : "");
        map.put("filter", context.getFilter() != null ? context.getFilter() : "");

        final Category category = context.getCategory();
        if (category != null) {
            map.put("category", category.getId());
            map.put("category_id", category.getId());
            map.put("category_name", category.getDisplayName() != null ? category.getDisplayName() : category.getId());
        }

        final Pet pet = context.getPet();
        if (pet != null) {
            map.put("pet_id", pet.getId());
            map.put("pet", pet.getId());
            String petName = pet.getIcon() != null && pet.getIcon().hasItemMeta()
                    ? Utils.stripColors(pet.getIcon().getItemMeta().getDisplayName())
                    : pet.getId();
            if (pet.getCurrentName() != null) {
                petName = Utils.stripColors(pet.getCurrentName());
            }
            map.put("pet_name", petName != null ? petName : pet.getId());
            if (pet.getOwner() != null) {
                final String ownerName = Bukkit.getOfflinePlayer(pet.getOwner()).getName();
                map.put("owner", ownerName != null ? ownerName : pet.getOwner().toString());
                map.put("owner_uuid", pet.getOwner().toString());
            }
            if (pet.getPetStats() != null) {
                map.put("pet_level", String.valueOf(pet.getPetStats().getCurrentLevel() != null
                        ? pet.getPetStats().getCurrentLevel().getLevelId() : 0));
                map.put("pet_health", String.format(Locale.US, "%.1f", pet.getPetStats().getCurrentHealth()));
                map.put("pet_max_health", String.format(Locale.US, "%.1f",
                        pet.getPetStats().getCurrentLevel() != null
                                ? pet.getPetStats().getCurrentLevel().getMaxHealth() : 0));
                map.put("pet_exp", String.format(Locale.US, "%.1f", pet.getPetStats().getExperience()));
            }
        }
        map.putAll(context.getExtras());
        return map;
    }
}
