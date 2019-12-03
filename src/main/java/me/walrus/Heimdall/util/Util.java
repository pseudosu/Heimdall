package me.walrus.Heimdall.util;

import me.walrus.Heimdall.Heimdall;
import me.walrus.Heimdall.network.UserData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

import java.util.Random;

public class Util {

    public static String getRandomHeimdallQuote() {
        return Config.heimdallQuotes.get(new Random().nextInt(Config.heimdallQuotes.size()));
    }

    public static String formatStaffChat(String playername, String nickname, String message) {
        return ChatColor.translateAlternateColorCodes('&', Heimdall.getConfigManager().STAFF_CHAT_FORMAT
                .replaceAll("%playername%", playername)
                .replaceAll("%playernickname%", nickname)
                .replaceAll("%message%", message));
    }

    public static String formatStaffChatToDiscord(String playername, String nickname, String message) {
        return Heimdall.getConfigManager().STAFF_CHAT_TO_DISCORD_FORMAT
                .replaceAll("%playername%", playername)
                .replaceAll("%playernickname%", nickname)
                .replaceAll("%message%", message);
    }

    public static String formatDiscordStaffChat(String name, String nickname, String message) {
        return Heimdall.getConfigManager().STAFF_CHAT_DISCORD_FORMAT
                .replaceAll("%playername%", name)
                .replaceAll("%playernickname%", nickname)
                .replaceAll("%message%", message);
    }

    public static void sendStaff(String message, boolean prefix) {
        for (Player p : Bukkit.getServer().getOnlinePlayers())
            if (p.hasPermission(Permissions.PERMISSION_ISSTAFF)) {
                if (prefix) {
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', Heimdall.getConfigManager().PREFIX + message));
                } else {
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                }
            }
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    public static void sendMessage(Player p, String message) {
        String prefix = Heimdall.getConfigManager().PREFIX;

        p.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + message));
    }

    public static String getSentence(String[] array, int startPoint) {
        StringBuilder message = new StringBuilder();
        for (int i = startPoint; i < array.length; i++) {
            message.append(array[i]);
            if ((i + 1) != array.length) {
                message.append(" ");
            }
        }
        return message.toString();
    }

    public static User getDiscordUser(String id) {
        String name = id.split("#")[0];
        String descriminator = id.split("#")[1];
        Server server = Heimdall.getDiscord().getServerById(Heimdall.getConfigManager().BOT_SERVER_ID).get();
        if (server.getMemberByNameAndDiscriminatorIgnoreCase(name, descriminator).isPresent()) {
            return server.getMemberByNameAndDiscriminatorIgnoreCase(name, descriminator).get();
        }
        return null;
    }

    public static User getDiscorduserByID(String id) {
        Server server = Heimdall.getDiscord().getServerById(Heimdall.getConfigManager().BOT_SERVER_ID).get();
        if (server.getMemberById(id).isPresent()) {
            return server.getMemberById(id).get();
        }
        return null;
    }

    public static void sendHelp(Player player) {
        if (player.hasPermission("ticket.list"))
            Util.sendMessage(player, "&f/ticket &alist&f; list your open tickets");
        if (player.hasPermission("ticket.create"))
            Util.sendMessage(player, "&f/ticket &acreate &9<issue>&f; create a ticket");
        if (player.hasPermission("ticket.close"))
            Util.sendMessage(player, "&f/ticket &aclose &9<ticket#>&f; close a ticket you own");
        if (player.hasPermission("ticket.link"))
            Util.sendMessage(player, "&f/ticket &alink&f; link your Discord account.");
        if (player.hasPermission("ticket.admin"))
            Util.sendMessage(player, "&f/ticket &aadmin&f; access admin functions.");
        if (player.hasPermission("ticket.verify"))
            Util.sendMessage(player, "&f/ticket &averify &9example#1234&f; verify your discord account");
    }

    public static void sendDiscordMessage(String message, String channel) {
        Heimdall.getDiscord().getServerById(Heimdall.getConfigManager().BOT_SERVER_ID).get()
                .getTextChannelById(channel).get()
                .sendMessage(message);
    }

    public static void sendEmbed(EmbedBuilder embed, String channel) {
        Heimdall.getDiscord().getServerById(Heimdall.getConfigManager().BOT_SERVER_ID).get()
                .getTextChannelById(channel).get()
                .sendMessage(embed);
    }

    public static boolean checkTickets(Player player) {
        if (Heimdall.getUser(player) == null)
            Heimdall.addUser(new UserData(player));
        UserData userTicketData = Heimdall.getUser(player);
        if (userTicketData.getActiveTickets().size() == 0) {
            return false;
        }
        return true;
    }
}
