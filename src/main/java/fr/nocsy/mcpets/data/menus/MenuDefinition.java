package fr.nocsy.mcpets.data.menus;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Getter
public class MenuDefinition {

    public enum Type {
        CHEST,
        PAGINATED,
        STORAGE,
        HOPPER
    }

    private final String id;
    private final String title;
    private final int rows;
    private final Type type;
    private final List<Integer> contentSlots;
    private final List<MenuItemDefinition> items;
    private final Material fillMaterial;
    private final String fillName;

    public MenuDefinition(final String id, final FileConfiguration config) {
        this.id = id;
        this.title = config.getString("title", "<white>Menu</white>");
        this.rows = Math.max(1, Math.min(6, config.getInt("rows", 6)));
        Type parsed = Type.CHEST;
        try {
            parsed = Type.valueOf(config.getString("type", "CHEST").toUpperCase());
        } catch (IllegalArgumentException ignored) {
        }
        this.type = parsed;
        this.contentSlots = parseContentSlots(config);
        this.items = MenuItemDefinition.fromSection(config.getConfigurationSection("items"));
        Material fill = null;
        if (config.contains("fill.material")) {
            try {
                fill = Material.valueOf(config.getString("fill.material").toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }
        this.fillMaterial = fill;
        this.fillName = config.getString("fill.name", " ");
    }

    public static MenuDefinition load(final String id, final File file) {
        final YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        return new MenuDefinition(id, config);
    }

    /**
     * Accepts:
     * <pre>
     * content-slots: [0, 1, 2]
     * content-slots: 0-44
     * content-slots: "10,11,12,20-24"
     * </pre>
     */
    static List<Integer> parseContentSlots(final FileConfiguration config) {
        final Set<Integer> slots = new LinkedHashSet<>();
        if (!config.contains("content-slots") && !config.contains("content_slots")) {
            return new ArrayList<>();
        }
        final String key = config.contains("content-slots") ? "content-slots" : "content_slots";
        if (config.isList(key)) {
            final List<?> raw = config.getList(key);
            if (raw != null) {
                for (final Object entry : raw) {
                    if (entry instanceof Number number) {
                        slots.add(number.intValue());
                    } else if (entry != null) {
                        slots.addAll(parseSlotToken(entry.toString()));
                    }
                }
            }
        } else if (config.isInt(key)) {
            slots.add(config.getInt(key));
        } else {
            final String asString = config.getString(key);
            if (asString != null && !asString.isBlank()) {
                slots.addAll(parseSlotToken(asString));
            }
        }
        return new ArrayList<>(slots);
    }

    static List<Integer> parseSlotToken(final String token) {
        final List<Integer> slots = new ArrayList<>();
        if (token == null || token.isBlank()) {
            return slots;
        }
        for (final String part : token.split("[,\\s]+")) {
            if (part.isBlank()) {
                continue;
            }
            if (part.contains("-")) {
                final String[] bounds = part.split("-", 2);
                try {
                    final int from = Integer.parseInt(bounds[0].trim());
                    final int to = Integer.parseInt(bounds[1].trim());
                    final int start = Math.min(from, to);
                    final int end = Math.max(from, to);
                    for (int i = start; i <= end; i++) {
                        slots.add(i);
                    }
                } catch (NumberFormatException ignored) {
                }
            } else {
                try {
                    slots.add(Integer.parseInt(part.trim()));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return slots;
    }

    public List<Integer> resolveContentSlots() {
        if (contentSlots != null && !contentSlots.isEmpty()) {
            return contentSlots;
        }
        // Default: all slots except those used by static items
        final List<Integer> used = new ArrayList<>();
        for (final MenuItemDefinition item : items) {
            if (item.getSlot() >= 0) {
                used.add(item.getSlot());
            }
        }
        final List<Integer> slots = new ArrayList<>();
        final int size = rows * 9;
        for (int i = 0; i < size; i++) {
            if (!used.contains(i)) {
                slots.add(i);
            }
        }
        return slots;
    }

    public MenuItemDefinition findPreviousPageItem() {
        return items.stream().filter(MenuItemDefinition::isPaginationPrevious).findFirst().orElse(null);
    }

    public MenuItemDefinition findNextPageItem() {
        return items.stream().filter(MenuItemDefinition::isPaginationNext).findFirst().orElse(null);
    }
}
