package fr.nocsy.mcpets.data.menus;

import fr.nocsy.mcpets.data.Category;
import fr.nocsy.mcpets.data.Pet;
import fr.nocsy.mcpets.data.PetSkin;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Per-open context for menus: current pet/category/page and a BACK navigation stack.
 */
public class MenuContext {

    private static final Map<UUID, MenuContext> CONTEXTS = new HashMap<>();

    @Getter
    private final Player player;

    @Getter
    @Setter
    private Pet pet;

    @Getter
    @Setter
    private Category category;

    @Getter
    @Setter
    private PetSkin skin;

    @Getter
    @Setter
    private int page = 1;

    @Getter
    @Setter
    private String filter; // PET / MOUNT / null

    @Getter
    @Setter
    private String currentMenuId;

    @Getter
    private final Deque<String> history = new ArrayDeque<>();

    @Getter
    private final Map<String, String> extras = new HashMap<>();

    public MenuContext(final Player player) {
        this.player = player;
    }

    public static MenuContext of(final Player player) {
        return CONTEXTS.computeIfAbsent(player.getUniqueId(), id -> new MenuContext(player));
    }

    public static void clear(final Player player) {
        CONTEXTS.remove(player.getUniqueId());
    }

    public void push(final String menuId) {
        if (menuId != null && !menuId.equals(currentMenuId)) {
            if (currentMenuId != null) {
                history.push(currentMenuId);
            }
            currentMenuId = menuId;
        }
    }

    @Nullable
    public String popBack() {
        if (history.isEmpty()) {
            return null;
        }
        currentMenuId = history.pop();
        return currentMenuId;
    }

    public MenuContext withPet(final Pet pet) {
        this.pet = pet;
        return this;
    }

    public MenuContext withCategory(final Category category) {
        this.category = category;
        return this;
    }

    public MenuContext withPage(final int page) {
        this.page = Math.max(1, page);
        return this;
    }

    public MenuContext withFilter(final String filter) {
        this.filter = filter;
        return this;
    }
}
