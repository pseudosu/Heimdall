package me.walrus.Heimdall.commands;

import me.walrus.Heimdall.Heimdall;
import me.walrus.Heimdall.util.Permissions;
import me.walrus.Heimdall.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CmdStaffChat implements CommandExecutor {


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            if (args.length < 1) {
                Bukkit.getConsoleSender().sendMessage("You can't toggle Staffchat from console.");
                return false;
            } else {
                String message = ChatColor.translateAlternateColorCodes('&', Util.getSentence(args, 0));
                message = ChatColor.stripColor(message);
                Util.sendStaff(Util.formatStaffChat("CONSOLE", "CONSOLE", message), false);
                Util.sendDiscordMessage(Util.formatStaffChatToDiscord("CONSOLE", "CONSOLE", message), Heimdall.getConfigManager().BOT_STAFF_CHAT);
                return true;
            }
        }
        Player player = (Player) sender;
        if (player.hasPermission(Permissions.PERMISSION_STAFFCHAT)) {
            if (args.length < 1) {
                if (Heimdall.staffChatUsers.contains(player)) {
                    Util.sendMessage(player, "&cStaff chat disabled. &7/" + label + "&c to &aenable.");
                    Heimdall.staffChatUsers.remove(player);
                    return true;

                }
                Heimdall.staffChatUsers.add(player);
                Util.sendMessage(player, "&aStaff chat enabled. &7/" + label + "&a to &cdisable.");
                return true;
            } else {
                String message = ChatColor.translateAlternateColorCodes('&', Util.getSentence(args, 0));
                message = ChatColor.stripColor(message);
                String name = ChatColor.stripColor(player.getName());
                String nick = ChatColor.stripColor(player.getDisplayName());

                Util.sendStaff(Util.formatStaffChat(player.getName(), player.getDisplayName(), message), false);
                Util.sendDiscordMessage(Util.formatStaffChatToDiscord(name, nick, message), Heimdall.getConfigManager().BOT_STAFF_CHAT);
            }
        }
        return false;
    }
}
