package fr.nocsy.mcpets.commands.mcpets;

import fr.nocsy.mcpets.PPermission;
import fr.nocsy.mcpets.commands.AArgument;
import fr.nocsy.mcpets.data.menus.MenuContext;
import fr.nocsy.mcpets.data.menus.MenuService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ArgumentEditor extends AArgument {

    public ArgumentEditor(CommandSender sender, String[] args) {
        super("editor", new int[]{1}, sender, args, "/mcpets editor");
    }

    @Override
    public boolean additionalConditions() {
        return sender instanceof Player && sender.hasPermission(PPermission.ADMIN.getPermission());
    }

    @Override
    public void commandEffect() {
        final Player p = (Player) sender;
        final MenuService menus = MenuService.getInstance();
        if (menus.resolve("editor-global", "global") != null) {
            final String id = menus.get("editor-global") != null ? "editor-global" : "global";
            menus.open(id, MenuContext.of(p));
        } else {
            fr.nocsy.mcpets.data.editor.Editor.getEditor(p).openEditor();
        }
    }
}
