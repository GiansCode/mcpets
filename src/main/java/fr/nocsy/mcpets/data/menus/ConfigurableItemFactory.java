package fr.nocsy.mcpets.data.menus;

import fr.nocsy.mcpets.utils.Utils;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.stream.Collectors;

public final class ConfigurableItemFactory {

    private ConfigurableItemFactory() {
    }

    public static ItemStack build(final MenuItemDefinition def, final MenuContext context) {
        if (def == null) {
            return new ItemStack(Material.AIR);
        }
        ItemStack stack;
        if (def.getSkullBase64() != null && !def.getSkullBase64().isEmpty()) {
            final List<Component> lore = MenuPlaceholders.apply(def.getLore(), context).stream()
                    .map(Utils::toComponent)
                    .collect(Collectors.toList());
            stack = Utils.createHead(MenuPlaceholders.apply(def.getName(), context), lore, def.getSkullBase64());
        } else {
            stack = new ItemStack(def.getMaterial(), def.getAmount());
            final ItemMeta meta = stack.getItemMeta();
            if (meta != null) {
                if (def.getName() != null && !def.getName().isEmpty()) {
                    meta.displayName(Utils.toComponent(MenuPlaceholders.apply(def.getName(), context)));
                }
                if (def.getLore() != null && !def.getLore().isEmpty()) {
                    meta.lore(MenuPlaceholders.apply(def.getLore(), context).stream()
                            .map(Utils::toComponent)
                            .collect(Collectors.toList()));
                }
                if (def.getCustomModelData() != null) {
                    meta.setCustomModelData(def.getCustomModelData());
                }
                if (def.isGlow()) {
                    meta.addEnchant(Enchantment.LUCK_OF_THE_SEA, 1, true);
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                }
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                stack.setItemMeta(meta);
            }
        }
        return stack;
    }

    public static ItemStack applyText(final ItemStack base, final String name, final List<String> lore, final MenuContext context) {
        if (base == null) {
            return null;
        }
        final ItemStack clone = base.clone();
        final ItemMeta meta = clone.getItemMeta();
        if (meta == null) {
            return clone;
        }
        if (name != null) {
            meta.displayName(Utils.toComponent(MenuPlaceholders.apply(name, context)));
        }
        if (lore != null && !lore.isEmpty()) {
            meta.lore(MenuPlaceholders.apply(lore, context).stream().map(Utils::toComponent).collect(Collectors.toList()));
        }
        clone.setItemMeta(meta);
        return clone;
    }
}
