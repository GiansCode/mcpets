package fr.nocsy.mcpets.data.menus;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public class MenuItemDefinition {

    private final String id;
    private final int slot;
    private final Material material;
    private final int amount;
    private final String name;
    private final List<String> lore;
    private final Integer customModelData;
    private final boolean glow;
    private final String skullBase64;
    private final String action;
    private final String condition;
    private final boolean paginationPrevious;
    private final boolean paginationNext;
    private final boolean contentSlot; // reserved for dynamic content area markers

    public MenuItemDefinition(final String id, final ConfigurationSection section) {
        this.id = id;
        this.slot = section.getInt("slot", -1);
        Material mat = Material.STONE;
        try {
            mat = Material.valueOf(section.getString("material", "STONE").toUpperCase());
        } catch (IllegalArgumentException ignored) {
        }
        this.material = mat;
        this.amount = Math.max(1, section.getInt("amount", 1));
        this.name = section.getString("name", "");
        this.lore = section.getStringList("lore") != null ? new ArrayList<>(section.getStringList("lore")) : new ArrayList<>();
        this.customModelData = section.contains("custom-model-data") ? section.getInt("custom-model-data") : null;
        this.glow = section.getBoolean("glow", false);
        this.skullBase64 = section.getString("skull", null);
        this.action = section.getString("action", "NONE");
        this.condition = section.getString("condition", null);
        this.paginationPrevious = section.getBoolean("pagination-previous", false)
                || "PREVIOUS_PAGE".equalsIgnoreCase(action);
        this.paginationNext = section.getBoolean("pagination-next", false)
                || "NEXT_PAGE".equalsIgnoreCase(action);
        this.contentSlot = section.getBoolean("content", false);
    }

    public static List<MenuItemDefinition> fromSection(final ConfigurationSection items) {
        if (items == null) {
            return Collections.emptyList();
        }
        final List<MenuItemDefinition> list = new ArrayList<>();
        for (final String key : items.getKeys(false)) {
            final ConfigurationSection section = items.getConfigurationSection(key);
            if (section != null) {
                list.add(new MenuItemDefinition(key, section));
            }
        }
        return list;
    }
}
