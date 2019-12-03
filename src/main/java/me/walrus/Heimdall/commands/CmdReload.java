package me.walrus.Heimdall.commands;

import me.walrus.Heimdall.Heimdall;
import me.walrus.Heimdall.util.Permissions;
import me.walrus.Heimdall.util.Util;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CmdReload implements CommandExecutor {

    Heimdall p;

    public CmdReload(Heimdall p) {
        this.p = p;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission(Permissions.PERMISSION_ADMIN)) {
            if (sender instanceof Player) {
                if (p.reloadHeimdall())
                    Util.sendMessage((Player) sender, "&aHeimdall reloaded.");
                else
                    Util.sendMessage((Player) sender, "&cError while reloading Heimdall, check console for errors.");
                return true;
            } else {
                if (p.reloadHeimdall())
                    sender.sendMessage("Heimdall reloaded.");
                else
                    sender.sendMessage("Error while reloading Heimdall, check console for errors.");
                return true;
            }
        }
        return false;
    }
}
