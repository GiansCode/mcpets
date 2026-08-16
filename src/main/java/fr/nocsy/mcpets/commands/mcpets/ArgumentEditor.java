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
        // Prefer YAML-driven global editor menu when available
        if (MenuService.getInstance().get("global") != null) {
            MenuService.getInstance().open("global", MenuContext.of(p));
        } else {
            fr.nocsy.mcpets.data.editor.Editor.getEditor(p).openEditor();
        }
    }
}
