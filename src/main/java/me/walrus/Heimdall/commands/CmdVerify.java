package me.walrus.Heimdall.commands;

import me.walrus.Heimdall.Heimdall;
import me.walrus.Heimdall.network.UserData;
import me.walrus.Heimdall.util.Permissions;
import me.walrus.Heimdall.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CmdVerify implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player))
            return false;
        Player player = (Player) sender;
        UserData userData = Heimdall.getUser(player);
        if (player.hasPermission(Permissions.PERMISSION_BASE)) {
            if (player.hasPermission(Permissions.PERMISSION_VERIFY)) {
                if (userData.isVerified()) {
                    Util.sendMessage(player, "You are already verified! Use /ticket to get started.");
                    userData.setVerified(true);
                    return true;
                }
                if (args.length >= 1) {
                    try {
                        int code = Integer.parseInt(args[0]);
                        if (Heimdall.pendingVerifications.containsKey(userData)) {
                            int secretCode = Heimdall.pendingVerifications.get(userData);
                            if (code == secretCode) {
                                Util.sendMessage(player, "&aYou are now verified! Use /ticket to get started.");
                                Bukkit.getServer().broadcastMessage("Their DiscordID is" + userData.getDiscordId());
                                userData.setVerified(true);
                                userData.setMySQLDiscordID(userData.getDiscordId());
                                userData.generateMySQLData();
                                Heimdall.pendingVerifications.remove(userData);
                                return true;
                            } else {
                                Util.sendMessage(player, "&cThat code was incorrect.");
                            }
                        } else {
                            Util.sendMessage(player, "&cPlease do /ticket verify example#1234 first!");
                            return false;
                        }
                    } catch (NumberFormatException e) {
                        if (Heimdall.pendingVerifications.containsKey(userData))
                            Util.sendMessage(player, "&7" + args[0] + "&c is not a valid code!");
                        else
                            Util.sendMessage(player, "&cPlease do /ticket verify example#1234 first!");
                        return false;
                    }
                } else {
                    Util.sendMessage(player, "&cIncorrect format. Please use /verify <code>");
                }
            }
            return false;
        }
        return false;
    }
}