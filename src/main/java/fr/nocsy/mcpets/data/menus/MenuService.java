package fr.nocsy.mcpets.data.menus;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import fr.nocsy.mcpets.MCPets;
import fr.nocsy.mcpets.data.Category;
import fr.nocsy.mcpets.data.CategoryType;
import fr.nocsy.mcpets.data.Items;
import fr.nocsy.mcpets.data.Pet;
import fr.nocsy.mcpets.data.PetSkin;
import fr.nocsy.mcpets.data.config.AbstractConfig;
import fr.nocsy.mcpets.data.config.GlobalConfig;
import fr.nocsy.mcpets.data.config.Language;
import fr.nocsy.mcpets.data.editor.Editor;
import fr.nocsy.mcpets.data.editor.EditorState;
import fr.nocsy.mcpets.data.sql.PlayerData;
import fr.nocsy.mcpets.utils.Utils;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Loads menus/*.yml and opens Triumph GUIs.
 */
public class MenuService {

    @Getter
    private static final MenuService instance = new MenuService();

    private final Map<String, MenuDefinition> menus = new HashMap<>();

    private MenuService() {
    }

    public void init() {
        reload();
    }

    public void reload() {
        menus.clear();
        final File folder = new File(AbstractConfig.getPath() + "menus/");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        saveDefaults(folder);
        loadFolder(folder);
        final File editorFolder = new File(folder, "editor");
        if (editorFolder.exists()) {
            loadFolder(editorFolder);
        }
        MCPets.getLog().info("[MCPets] Loaded " + menus.size() + " menu definition(s).");
    }

    private void saveDefaults(final File folder) {
        final String[] defaults = {
                "pets.yml", "mounts.yml", "categories.yml", "category.yml",
                "pet-interaction.yml", "mount-interaction.yml", "skins.yml", "pet-inventory.yml",
                "editor/global.yml", "editor/config.yml", "editor/pets.yml"
        };
        for (final String name : defaults) {
            final File target = new File(folder, name);
            if (target.exists()) {
                continue;
            }
            target.getParentFile().mkdirs();
            try (InputStream in = MCPets.getInstance().getResource("menus/" + name)) {
                if (in != null) {
                    Files.copy(in, target.toPath());
                } else {
                    // Fallback empty stub so something exists
                    final YamlConfiguration cfg = new YamlConfiguration();
                    cfg.set("title", "<white>" + name + "</white>");
                    cfg.set("rows", 6);
                    cfg.set("type", "CHEST");
                    cfg.save(target);
                }
            } catch (final Exception ex) {
                MCPets.getLog().log(Level.WARNING, "Could not save default menu " + name, ex);
            }
        }
    }

    private void loadFolder(final File folder) {
        final File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) {
            return;
        }
        for (final File file : files) {
            final String id = file.getName().replace(".yml", "");
            try {
                menus.put(id, MenuDefinition.load(id, file));
            } catch (final Exception ex) {
                MCPets.getLog().log(Level.SEVERE, "Failed to load menu " + file.getName(), ex);
            }
        }
    }

    public MenuDefinition get(final String id) {
        return menus.get(id);
    }

    public void openPets(final Player player) {
        PlayerData.get(player.getUniqueId());
        if (!Category.getCategories().isEmpty()) {
            openCategories(player, "PET");
            return;
        }
        final MenuContext ctx = MenuContext.of(player).withPage(1).withFilter("PET");
        open("pets", ctx);
    }

    public void openMounts(final Player player) {
        PlayerData.get(player.getUniqueId());
        final List<Category> mountCats = Category.getCategories(CategoryType.MOUNT);
        if (!mountCats.isEmpty()) {
            openCategories(player, "MOUNT");
            return;
        }
        final MenuContext ctx = MenuContext.of(player).withPage(1).withFilter("MOUNT");
        open("mounts", ctx);
    }

    public void openCategories(final Player player, final String filter) {
        final MenuContext ctx = MenuContext.of(player).withPage(1).withFilter(filter);
        open("categories", ctx);
    }

    public void openInteraction(final Player player, final Pet pet, final boolean mount) {
        if (pet.getTamingProgress() < 1) {
            return;
        }
        pet.setOwner(player.getUniqueId());
        final MenuContext ctx = MenuContext.of(player).withPet(pet)
                .withFilter(mount ? "MOUNT" : "PET");
        open(mount ? "mount-interaction" : "pet-interaction", ctx);
    }

    public void openSkins(final Player player, final Pet pet) {
        final MenuContext ctx = MenuContext.of(player).withPet(pet);
        open("skins", ctx);
    }

    public void openEditor(final Player player, final String stateName) {
        try {
            final EditorState state = EditorState.valueOf(stateName);
            final Editor editor = Editor.getEditor(player);
            editor.setState(state);
            editor.openEditor();
        } catch (IllegalArgumentException ex) {
            open("global", MenuContext.of(player));
        }
    }

    public void openBack(final MenuContext context) {
        final String back = context.popBack();
        if (back == null) {
            context.getPlayer().closeInventory();
            return;
        }
        open(back, context);
    }

    public void reopen(final MenuContext context) {
        if (context.getCurrentMenuId() != null) {
            open(context.getCurrentMenuId(), context);
        }
    }

    public void open(final String menuId, final MenuContext context) {
        final MenuDefinition def = menus.get(menuId);
        if (def == null) {
            MCPets.getLog().warning("[MCPets] Unknown menu: " + menuId);
            return;
        }
        context.push(menuId);
        context.getExtras().put("menu_title", MenuPlaceholders.apply(def.getTitle(), context));

        switch (menuId) {
            case "pets" -> openPetList(def, context, false);
            case "mounts" -> openPetList(def, context, true);
            case "categories" -> openCategoriesGui(def, context);
            case "category" -> openCategoryPets(def, context);
            case "pet-interaction", "mount-interaction" -> openInteractionGui(def, context);
            case "skins" -> openSkinsGui(def, context);
            default -> openStaticGui(def, context);
        }
    }

    private int resolveRows(final MenuDefinition def) {
        final int configured = GlobalConfig.getInstance().getAdaptiveInventory();
        if (configured > 0) {
            return Math.max(1, Math.min(6, configured / 9));
        }
        return def.getRows();
    }

    private Component title(final MenuDefinition def, final MenuContext context) {
        return Utils.toComponent(MenuPlaceholders.apply(def.getTitle(), context));
    }

    private void placeStaticItems(final Gui gui, final MenuDefinition def, final MenuContext context,
                                  final boolean hasPrevious, final boolean hasNext) {
        for (final MenuItemDefinition itemDef : def.getItems()) {
            if (itemDef.isContentSlot()) {
                continue;
            }
            if (itemDef.isPaginationPrevious() && !hasPrevious) {
                continue;
            }
            if (itemDef.isPaginationNext() && !hasNext) {
                continue;
            }
            if (!MenuActions.matchesCondition(itemDef.getCondition(), context)) {
                continue;
            }
            if (itemDef.getSlot() < 0) {
                continue;
            }
            final ItemStack stack = ConfigurableItemFactory.build(itemDef, context);
            final GuiItem guiItem = ItemBuilder.from(stack).asGuiItem(e -> {
                e.setCancelled(true);
                MenuActions.execute(itemDef.getAction(), context, e.getCurrentItem());
            });
            gui.setItem(itemDef.getSlot(), guiItem);
        }
        if (def.getFillMaterial() != null) {
            final GuiItem filler = ItemBuilder.from(def.getFillMaterial())
                    .name(Utils.toComponent(MenuPlaceholders.apply(def.getFillName(), context)))
                    .asGuiItem(e -> e.setCancelled(true));
            gui.getGuiItems().forEach((slot, item) -> {
            });
            for (int i = 0; i < def.getRows() * 9; i++) {
                if (gui.getGuiItem(i) == null) {
                    gui.setItem(i, filler);
                }
            }
        }
    }

    private void placeStaticItemsPaginated(final PaginatedGui gui, final MenuDefinition def, final MenuContext context,
                                          final boolean hasPrevious, final boolean hasNext) {
        for (final MenuItemDefinition itemDef : def.getItems()) {
            if (itemDef.isContentSlot()) {
                continue;
            }
            if (itemDef.isPaginationPrevious() && !hasPrevious) {
                continue;
            }
            if (itemDef.isPaginationNext() && !hasNext) {
                continue;
            }
            if (!MenuActions.matchesCondition(itemDef.getCondition(), context)) {
                continue;
            }
            if (itemDef.getSlot() < 0) {
                continue;
            }
            final ItemStack stack = ConfigurableItemFactory.build(itemDef, context);
            final GuiItem guiItem = ItemBuilder.from(stack).asGuiItem(e -> {
                e.setCancelled(true);
                if (itemDef.isPaginationPrevious()) {
                    gui.previous();
                    context.setPage(gui.getCurrentPageNum());
                    return;
                }
                if (itemDef.isPaginationNext()) {
                    gui.next();
                    context.setPage(gui.getCurrentPageNum());
                    return;
                }
                MenuActions.execute(itemDef.getAction(), context, e.getCurrentItem());
            });
            gui.setItem(itemDef.getSlot(), guiItem);
        }
    }

    private void openPetList(final MenuDefinition def, final MenuContext context, final boolean mountsOnly) {
        final Player player = context.getPlayer();
        List<Pet> pets = Pet.getAvailablePets(player);
        if (mountsOnly) {
            pets = pets.stream().filter(Pet::isMountable).toList();
        }

        final PaginatedGui gui = Gui.paginated()
                .title(title(def, context))
                .rows(resolveRows(def))
                .disableAllInteractions()
                .create();

        for (final Pet pet : pets) {
            final ItemStack icon = pet.buildItem(pet.getIcon(), true);
            gui.addItem(ItemBuilder.from(icon).asGuiItem(e -> {
                e.setCancelled(true);
                player.closeInventory();
                pet.copy().spawnWithMessage(player);
            }));
        }

        placeStaticItemsPaginated(gui, def, context, true, true);

        // Jump to requested page
        for (int i = 1; i < context.getPage(); i++) {
            if (!gui.next()) {
                break;
            }
        }
        gui.open(player);
    }

    private void openCategoriesGui(final MenuDefinition def, final MenuContext context) {
        final Player player = context.getPlayer();
        CategoryType type = CategoryType.PET;
        try {
            if (context.getFilter() != null) {
                type = CategoryType.valueOf(context.getFilter());
            }
        } catch (IllegalArgumentException ignored) {
        }
        final List<Category> categories = Category.getCategories(type);

        // Dynamic title for filter
        if ("MOUNT".equalsIgnoreCase(context.getFilter())) {
            context.getExtras().put("menu_title", Language.INVENTORY_MOUNTS_MENU.getMessage());
        } else if ("PET".equalsIgnoreCase(context.getFilter())) {
            context.getExtras().put("menu_title", Language.INVENTORY_PETS_MENU.getMessage());
        }

        final PaginatedGui gui = Gui.paginated()
                .title(Utils.toComponent(MenuPlaceholders.apply(
                        context.getExtras().getOrDefault("menu_title", def.getTitle()), context)))
                .rows(resolveRows(def))
                .disableAllInteractions()
                .create();

        for (final Category category : categories) {
            boolean owns = false;
            for (final Pet pet : category.getPets()) {
                if (pet.has(player)) {
                    owns = true;
                    break;
                }
            }
            if (!owns) {
                continue;
            }
            final ItemStack icon = category.getIcon().clone();
            gui.addItem(ItemBuilder.from(icon).asGuiItem(e -> {
                e.setCancelled(true);
                context.withCategory(category).withPage(1);
                open("category", context);
            }));
        }

        placeStaticItemsPaginated(gui, def, context, true, true);
        for (int i = 1; i < context.getPage(); i++) {
            if (!gui.next()) break;
        }
        gui.open(player);
    }

    private void openCategoryPets(final MenuDefinition def, final MenuContext context) {
        final Player player = context.getPlayer();
        final Category category = context.getCategory();
        if (category == null) {
            openCategories(player, context.getFilter() != null ? context.getFilter() : "PET");
            return;
        }
        context.getExtras().put("category_name", category.getDisplayName());

        final List<Pet> pets = new ArrayList<>();
        for (final Pet pet : category.getPets()) {
            if (pet.has(player)) {
                pets.add(pet);
            }
        }

        final PaginatedGui gui = Gui.paginated()
                .title(Utils.toComponent(MenuPlaceholders.apply(
                        category.getDisplayName() != null ? category.getDisplayName() : def.getTitle(), context)))
                .rows(resolveRows(def))
                .disableAllInteractions()
                .create();

        for (final Pet pet : pets) {
            final ItemStack icon = pet.buildItem(pet.getIcon(), true);
            gui.addItem(ItemBuilder.from(icon).asGuiItem(e -> {
                e.setCancelled(true);
                Category.unregisterPlayerView(player);
                player.closeInventory();
                pet.copy().spawnWithMessage(player);
            }));
        }

        // Outside-click back support via close + config
        gui.setDefaultClickAction(e -> e.setCancelled(true));
        if (GlobalConfig.getInstance().isEnableClickBackToMenu()) {
            gui.setCloseGuiAction(e -> {
                // no-op — outside click handled via BACK item
            });
        }

        placeStaticItemsPaginated(gui, def, context, true, true);
        Category.registerPlayerView(player, category);
        for (int i = 1; i < context.getPage(); i++) {
            if (!gui.next()) break;
        }
        gui.open(player);
    }

    private void openInteractionGui(final MenuDefinition def, final MenuContext context) {
        final Player player = context.getPlayer();
        final Pet pet = context.getPet();
        if (pet == null) {
            return;
        }

        final Gui gui = Gui.gui()
                .title(title(def, context))
                .rows(def.getRows())
                .disableAllInteractions()
                .create();

        for (final MenuItemDefinition itemDef : def.getItems()) {
            if (!MenuActions.matchesCondition(itemDef.getCondition(), context)) {
                continue;
            }
            if (itemDef.getSlot() < 0) {
                continue;
            }
            ItemStack stack;
            final String action = itemDef.getAction() != null ? itemDef.getAction().toUpperCase() : "";
            final String id = itemDef.getId() != null ? itemDef.getId().toLowerCase() : "";
            if ("REVOKE".equals(action) || "info".equals(id) || "pet_info".equals(id)) {
                stack = pet.buildItem(Items.petInfo(pet), true);
            } else if ("GIVE_SIGNAL_STICK".equals(action) && pet.getSignalStick() != null) {
                stack = pet.getSignalStick().clone();
            } else if ("BACK_TO_PET_MENU".equals(action) || ("back".equals(id) && !"mount-interaction".equals(context.getCurrentMenuId()))) {
                stack = overlayMenuItem(Items.PETMENU.getItem(), itemDef, context);
            } else if ("BACK_TO_MOUNT_MENU".equals(action)) {
                stack = overlayMenuItem(Items.MOUNTMENU.getItem(), itemDef, context);
            } else if ("OPEN_SKINS".equals(action) || "SKINS".equals(action)) {
                stack = overlayMenuItem(Items.SKINS.getItem(), itemDef, context);
            } else if ("RENAME".equals(action)) {
                stack = overlayMenuItem(Items.RENAME.getItem(), itemDef, context);
            } else if ("MOUNT".equals(action)) {
                stack = overlayMenuItem(Items.MOUNT.getItem(), itemDef, context);
            } else if ("OPEN_INVENTORY".equals(action) || "PET_INVENTORY".equals(action)) {
                stack = overlayMenuItem(Items.INVENTORY.getItem(), itemDef, context);
            } else if (itemDef.isPaginationPrevious()) {
                stack = overlayMenuItem(Items.PREVIOUS_PAGE_SELECTOR.getItem(), itemDef, context);
            } else if (itemDef.isPaginationNext()) {
                stack = overlayMenuItem(Items.NEXT_PAGE_SELECTOR.getItem(), itemDef, context);
            } else {
                stack = ConfigurableItemFactory.build(itemDef, context);
            }
            final GuiItem guiItem = ItemBuilder.from(stack).asGuiItem(e -> {
                e.setCancelled(true);
                MenuActions.execute(itemDef.getAction(), context, e.getCurrentItem());
            });
            gui.setItem(itemDef.getSlot(), guiItem);
        }
        gui.open(player);
    }

    private ItemStack overlayMenuItem(final ItemStack base, final MenuItemDefinition itemDef, final MenuContext context) {
        if (base == null) {
            return ConfigurableItemFactory.build(itemDef, context);
        }
        // Prefer menuIcons.yml / Items defaults, but allow YAML name/lore overrides
        if ((itemDef.getName() == null || itemDef.getName().isEmpty())
                && (itemDef.getLore() == null || itemDef.getLore().isEmpty())) {
            return base.clone();
        }
        return ConfigurableItemFactory.applyText(base.clone(),
                itemDef.getName() != null && !itemDef.getName().isEmpty() ? itemDef.getName() : null,
                itemDef.getLore() != null && !itemDef.getLore().isEmpty() ? itemDef.getLore() : null,
                context);
    }

    private void openSkinsGui(final MenuDefinition def, final MenuContext context) {
        final Player player = context.getPlayer();
        final Pet pet = context.getPet();
        if (pet == null || !pet.hasSkins()) {
            return;
        }
        final List<PetSkin> skins = new ArrayList<>();
        for (final PetSkin skin : PetSkin.getSkins(pet)) {
            if (skin.getPermission() == null || skin.getPermission().isEmpty() || player.hasPermission(skin.getPermission())) {
                skins.add(skin);
            }
        }
        if (skins.isEmpty()) {
            return;
        }

        final Gui gui = Gui.gui()
                .title(title(def, context))
                .rows(Math.min(6, Math.max(1, (int) Math.ceil(skins.size() / 9.0))))
                .disableAllInteractions()
                .create();

        for (final PetSkin skin : skins) {
            gui.addItem(ItemBuilder.from(skin.getIcon().clone()).asGuiItem(e -> {
                e.setCancelled(true);
                MenuActions.execute("APPLY_SKIN", context, e.getCurrentItem());
            }));
        }
        placeStaticItems(gui, def, context, false, false);
        player.setMetadata("MCPetsSkins", new FixedMetadataValue(MCPets.getInstance(), pet.getId()));
        gui.setCloseGuiAction(e -> player.removeMetadata("MCPetsSkins", MCPets.getInstance()));
        gui.open(player);
    }

    private void openStaticGui(final MenuDefinition def, final MenuContext context) {
        final Gui gui = Gui.gui()
                .title(title(def, context))
                .rows(def.getRows())
                .disableAllInteractions()
                .create();
        placeStaticItems(gui, def, context, false, false);
        gui.open(context.getPlayer());
    }
}
