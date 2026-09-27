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
import fr.nocsy.mcpets.data.editor.EditorEditing;
import fr.nocsy.mcpets.data.editor.EditorItems;
import fr.nocsy.mcpets.data.editor.EditorPageSelection;
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
        loadFolder(folder, "");
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
            target.getParentFile().mkdirs();
            try (InputStream in = MCPets.getInstance().getResource("menus/" + name)) {
                if (in == null) {
                    if (!target.exists()) {
                        final YamlConfiguration cfg = new YamlConfiguration();
                        cfg.set("title", "<white>" + name + "</white>");
                        cfg.set("rows", 6);
                        cfg.set("type", "CHEST");
                        cfg.save(target);
                    }
                    continue;
                }
                if (!target.exists()) {
                    Files.copy(in, target.toPath());
                    continue;
                }
                // Existing install: merge missing keys from jar so new options like content-slots appear
                final YamlConfiguration jarCfg = YamlConfiguration.loadConfiguration(
                        new java.io.InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8));
                final YamlConfiguration diskCfg = YamlConfiguration.loadConfiguration(target);
                boolean changed = false;
                if (!diskCfg.contains("content-slots") && !diskCfg.contains("content_slots")
                        && (jarCfg.contains("content-slots") || jarCfg.contains("content_slots"))) {
                    final String key = jarCfg.contains("content-slots") ? "content-slots" : "content_slots";
                    diskCfg.set("content-slots", jarCfg.get(key));
                    changed = true;
                }
                if (changed) {
                    diskCfg.save(target);
                    MCPets.getLog().info("[MCPets] Updated missing keys in menus/" + name);
                }
            } catch (final Exception ex) {
                MCPets.getLog().log(Level.WARNING, "Could not save default menu " + name, ex);
            }
        }
    }

    /**
     * Resolve a menu by trying several ids (e.g. editor-pets, then pets).
     */
    public MenuDefinition resolve(final String... ids) {
        for (final String id : ids) {
            if (id == null) {
                continue;
            }
            final MenuDefinition def = menus.get(id);
            if (def != null) {
                return def;
            }
        }
        return null;
    }

    /**
     * Load the pet-editor YAML from disk every time (authoritative for content-slots).
     */
    public MenuDefinition loadEditorPetsMenu() {
        final File file = new File(AbstractConfig.getPath() + "menus/editor/pets.yml");
        if (file.exists()) {
            try {
                final MenuDefinition fromDisk = MenuDefinition.load("editor-pets", file);
                menus.put("editor-pets", fromDisk);
                return fromDisk;
            } catch (final Exception ex) {
                MCPets.getLog().log(Level.WARNING, "Failed to load menus/editor/pets.yml", ex);
            }
        }
        return resolve("editor-pets", "pets");
    }

    private void loadFolder(final File folder, final String idPrefix) {
        final File[] files = folder.listFiles();
        if (files == null) {
            return;
        }
        for (final File file : files) {
            if (file.isDirectory()) {
                final String nextPrefix = idPrefix.isEmpty() ? file.getName() : idPrefix + "-" + file.getName();
                loadFolder(file, nextPrefix);
                continue;
            }
            if (!file.getName().endsWith(".yml")) {
                continue;
            }
            final String baseName = file.getName().substring(0, file.getName().length() - 4);
            final String id = idPrefix.isEmpty() ? baseName : idPrefix + "-" + baseName;
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
        MCPets.getLog().warning("[MCPets] EDITOR_NAVIGATE -> " + stateName);
        try {
            final EditorState state = EditorState.valueOf(stateName);
            final Editor editor = Editor.getEditor(player);
            editor.setState(state);
            editor.openEditor();
        } catch (IllegalArgumentException ex) {
            MCPets.getLog().warning("[MCPets] Unknown editor state: " + stateName);
            open(resolve("editor-global", "global") != null && get("editor-global") != null
                    ? "editor-global" : "global", MenuContext.of(player));
        }
    }

    /**
     * Opens the pet list editor using {@code menus/editor/pets.yml} content-slots.
     */
    public void openPetEditor(final Player player) {
        final MenuDefinition menuDef = loadEditorPetsMenu();
        final String title = menuDef != null ? menuDef.getTitle() : EditorState.PET_EDITOR.getMenuTitle();
        final int rows = menuDef != null ? menuDef.getRows() : 6;
        final int size = Math.max(9, Math.min(54, rows * 9));

        final List<Integer> contentSlots = menuDef != null
                ? menuDef.resolveContentSlots()
                : defaultEditorContentSlots(size);

        MCPets.getLog().warning("[MCPets] openPetEditor content-slots=" + contentSlots
                + " file=" + AbstractConfig.getPath() + "menus/editor/pets.yml"
                + " menuDef=" + (menuDef != null ? menuDef.getId() : "null"));
        // Quiet: no chat spam — slots apply from YAML silently

        final org.bukkit.inventory.Inventory inventory =
                new fr.nocsy.mcpets.data.inventories.PetInventoryHolder(
                        size, title, fr.nocsy.mcpets.data.inventories.PetInventoryHolder.Type.EDITOR_MENU)
                        .getInventory();

        int backSlot = Math.min(45, size - 1);
        int createSlot = Math.min(49, size - 1);
        int pageSlot = Math.min(53, size - 1);
        if (menuDef != null) {
            for (final MenuItemDefinition item : menuDef.getItems()) {
                if (item.getSlot() < 0 || item.getSlot() >= size) {
                    continue;
                }
                final String id = item.getId() != null ? item.getId().toLowerCase() : "";
                if ("back".equals(id)) {
                    backSlot = item.getSlot();
                } else if ("create".equals(id)) {
                    createSlot = item.getSlot();
                } else if ("page".equals(id) || item.isPaginationNext() || item.isPaginationPrevious()) {
                    pageSlot = item.getSlot();
                }
            }
        }
        inventory.setItem(backSlot, EditorItems.BACK_TO_GLOBAL_SELECTION.getItem());
        inventory.setItem(createSlot, EditorItems.PET_EDITOR_CREATE_NEW.getItem());
        inventory.setItem(pageSlot, EditorItems.PAGE_SELECTOR.getItem());

        for (int i = 0; i < size; i++) {
            if (inventory.getItem(i) != null || contentSlots.contains(Integer.valueOf(i))) {
                continue;
            }
            inventory.setItem(i, EditorItems.FILLER.getItem());
        }

        final EditorEditing editing = EditorEditing.get(player);
        editing.getEditorMapping().clear();
        final int page = EditorPageSelection.get(player);
        final int pageSize = Math.max(1, contentSlots.size());

        int visibleIndex = 0;
        for (final Pet pet : Pet.getObjectPets()) {
            if (EditorItems.getCachedDeleted().contains(pet.getId())) {
                continue;
            }
            if (visibleIndex < pageSize * page) {
                visibleIndex++;
                continue;
            }
            final int slotOffset = visibleIndex - pageSize * page;
            if (slotOffset >= contentSlots.size()) {
                break;
            }
            final int slot = contentSlots.get(slotOffset);
            if (slot >= 0 && slot < size) {
                inventory.setItem(slot, EditorItems.PET_EDITOR_EDIT_PET.setupPetIcon(pet.getId()).getItem());
                editing.getEditorMapping().put(slot, pet.getId());
            }
            visibleIndex++;
        }

        // Keep Editor state in sync for click listeners / back navigation
        final Editor editor = Editor.getEditor(player);
        editor.setState(EditorState.PET_EDITOR);
        player.openInventory(inventory);
    }

    private static List<Integer> defaultEditorContentSlots(final int size) {
        final List<Integer> slots = new ArrayList<>();
        final int contentEnd = Math.max(0, size - 9);
        for (int i = 0; i < contentEnd; i++) {
            slots.add(i);
        }
        return slots;
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
            case "global", "editor-global" -> openStaticGui(def, context);
            default -> openStaticGui(def, context);
        }
    }

    /**
     * Place dynamic items into YAML {@code content-slots} (or all free slots if unset).
     */
    public void placeContent(final org.bukkit.inventory.Inventory inventory,
                             final MenuDefinition def,
                             final List<ItemStack> content,
                             final int page) {
        final List<Integer> slots = def.resolveContentSlots();
        if (slots.isEmpty()) {
            return;
        }
        final int pageSize = slots.size();
        final int start = Math.max(0, page) * pageSize;
        for (int i = 0; i < pageSize; i++) {
            final int contentIndex = start + i;
            if (contentIndex >= content.size()) {
                break;
            }
            final int slot = slots.get(i);
            if (slot < 0 || slot >= inventory.getSize()) {
                continue;
            }
            inventory.setItem(slot, content.get(contentIndex));
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

        final List<Integer> contentSlots = resolvePagedContentSlots(def);
        final int pageSize = Math.max(1, contentSlots.size());
        final int totalPages = Math.max(1, (int) Math.ceil(pets.size() / (double) pageSize));
        final int page = Math.min(Math.max(1, context.getPage()), totalPages);
        context.setPage(page);

        final Gui gui = Gui.gui()
                .title(title(def, context))
                .rows(resolveRows(def))
                .disableAllInteractions()
                .create();

        final int start = (page - 1) * pageSize;
        for (int i = 0; i < pageSize && start + i < pets.size(); i++) {
            final Pet pet = pets.get(start + i);
            final int slot = contentSlots.get(i);
            final ItemStack icon = pet.buildItem(pet.getIcon(), true);
            gui.setItem(slot, ItemBuilder.from(icon).asGuiItem(e -> {
                e.setCancelled(true);
                player.closeInventory();
                pet.copy().spawnWithMessage(player);
            }));
        }

        placeStaticItems(gui, def, context, page > 1, page < totalPages);
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

        if ("MOUNT".equalsIgnoreCase(context.getFilter())) {
            context.getExtras().put("menu_title", Language.INVENTORY_MOUNTS_MENU.getMessage());
        } else if ("PET".equalsIgnoreCase(context.getFilter())) {
            context.getExtras().put("menu_title", Language.INVENTORY_PETS_MENU.getMessage());
        }

        final List<Category> owned = new ArrayList<>();
        for (final Category category : categories) {
            boolean owns = false;
            for (final Pet pet : category.getPets()) {
                if (pet.has(player)) {
                    owns = true;
                    break;
                }
            }
            if (owns) {
                owned.add(category);
            }
        }

        final List<Integer> contentSlots = resolvePagedContentSlots(def);
        final int pageSize = Math.max(1, contentSlots.size());
        final int totalPages = Math.max(1, (int) Math.ceil(owned.size() / (double) pageSize));
        final int page = Math.min(Math.max(1, context.getPage()), totalPages);
        context.setPage(page);

        final Gui gui = Gui.gui()
                .title(Utils.toComponent(MenuPlaceholders.apply(
                        context.getExtras().getOrDefault("menu_title", def.getTitle()), context)))
                .rows(resolveRows(def))
                .disableAllInteractions()
                .create();

        final int start = (page - 1) * pageSize;
        for (int i = 0; i < pageSize && start + i < owned.size(); i++) {
            final Category category = owned.get(start + i);
            final int slot = contentSlots.get(i);
            final ItemStack icon = category.getIcon().clone();
            gui.setItem(slot, ItemBuilder.from(icon).asGuiItem(e -> {
                e.setCancelled(true);
                context.withCategory(category).withPage(1);
                open("category", context);
            }));
        }

        placeStaticItems(gui, def, context, page > 1, page < totalPages);
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

        final List<Integer> contentSlots = resolvePagedContentSlots(def);
        final int pageSize = Math.max(1, contentSlots.size());
        final int totalPages = Math.max(1, (int) Math.ceil(pets.size() / (double) pageSize));
        final int page = Math.min(Math.max(1, context.getPage()), totalPages);
        context.setPage(page);

        final Gui gui = Gui.gui()
                .title(Utils.toComponent(MenuPlaceholders.apply(
                        category.getDisplayName() != null ? category.getDisplayName() : def.getTitle(), context)))
                .rows(resolveRows(def))
                .disableAllInteractions()
                .create();

        final int start = (page - 1) * pageSize;
        for (int i = 0; i < pageSize && start + i < pets.size(); i++) {
            final Pet pet = pets.get(start + i);
            final int slot = contentSlots.get(i);
            final ItemStack icon = pet.buildItem(pet.getIcon(), true);
            gui.setItem(slot, ItemBuilder.from(icon).asGuiItem(e -> {
                e.setCancelled(true);
                Category.unregisterPlayerView(player);
                player.closeInventory();
                pet.copy().spawnWithMessage(player);
            }));
        }

        gui.setDefaultClickAction(e -> e.setCancelled(true));
        placeStaticItems(gui, def, context, page > 1, page < totalPages);
        Category.registerPlayerView(player, category);
        gui.open(player);
    }

    /**
     * Content slots for paged menus, clipped to the resolved inventory size.
     */
    private List<Integer> resolvePagedContentSlots(final MenuDefinition def) {
        final int size = resolveRows(def) * 9;
        final List<Integer> raw = def.resolveContentSlots();
        final List<Integer> slots = new ArrayList<>();
        for (final Integer slot : raw) {
            if (slot != null && slot >= 0 && slot < size) {
                slots.add(slot);
            }
        }
        if (!slots.isEmpty()) {
            return slots;
        }
        // Absolute fallback: everything except known static item slots
        final List<Integer> used = new ArrayList<>();
        for (final MenuItemDefinition item : def.getItems()) {
            if (item.getSlot() >= 0) {
                used.add(item.getSlot());
            }
        }
        for (int i = 0; i < size; i++) {
            if (!used.contains(i)) {
                slots.add(i);
            }
        }
        return slots;
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
