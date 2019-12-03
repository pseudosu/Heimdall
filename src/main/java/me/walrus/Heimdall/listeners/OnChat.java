package me.walrus.Heimdall.listeners;

import me.walrus.Heimdall.Heimdall;
import me.walrus.Heimdall.util.Permissions;
import me.walrus.Heimdall.util.Util;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class OnChat implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void staffChatEvent(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission(Permissions.PERMISSION_STAFFCHAT)) {
            if (Heimdall.staffChatUsers.contains(event.getPlayer())) {
                event.setCancelled(true);
                String message = ChatColor.translateAlternateColorCodes('&', event.getMessage());
                message = ChatColor.stripColor(message);
                String name = ChatColor.stripColor(event.getPlayer().getName());
                String nick = ChatColor.stripColor(event.getPlayer().getDisplayName());
                Util.sendStaff(Util.formatStaffChat(player.getName(), player.getDisplayName(), event.getMessage()), false);
                Util.sendDiscordMessage(Util.formatStaffChatToDiscord(name, nick, message), Heimdall.getConfigManager().BOT_STAFF_CHAT);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onCommandExecute(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String command = event.getMessage();
        if (player.hasPermission(Permissions.PERMISSION_LOGCOMMANDS) && !player.hasPermission(Permissions.PERMISSION_LOGCOMMANDSEXEMPT)) {
            Util.sendDiscordMessage(player.getName() + "(" + ChatColor.stripColor(event.getPlayer().getDisplayName()) + ") executed command: " + command, Heimdall.getConfigManager().BOT_COMMAND_LOG_CHANNEL_ID);
            Util.sendStaff(ChatColor.DARK_GRAY + event.getPlayer().getName() + "&8 ran command: &7&o" + command, true);
        }
    }
}
