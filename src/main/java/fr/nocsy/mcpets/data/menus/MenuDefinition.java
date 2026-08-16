package fr.nocsy.mcpets.data.menus;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        this.contentSlots = config.getIntegerList("content-slots");
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
