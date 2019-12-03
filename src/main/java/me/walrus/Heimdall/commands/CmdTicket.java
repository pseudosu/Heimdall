package me.walrus.Heimdall.commands;

import me.walrus.Heimdall.Heimdall;
import me.walrus.Heimdall.network.Network;
import me.walrus.Heimdall.network.Ticket;
import me.walrus.Heimdall.network.UserData;
import me.walrus.Heimdall.util.MyTimer;
import me.walrus.Heimdall.util.Permissions;
import me.walrus.Heimdall.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.javacord.api.entity.user.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

public class CmdTicket implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player))
            return false;
        Player player = (Player) sender;
        if (player.hasPermission(Permissions.PERMISSION_BASE)) {
            UserData userData = Heimdall.getUser(player);
            /*
            This block checks if their discord account is not verified.
             */

            if (userData.getLocalDiscordUser() == null) {
                if (player.hasPermission(Permissions.PERMISSION_VERIFY)) {
                    if (args.length == 0) {
                        Util.sendMessage(player, "&cYou must verify your Discord account first!");
                        Util.sendMessage(player, "&cPlease type /ticket verify example#1234");
                        return false;
                    }

                    if (args[0].equals("verify")) {
                        if (!(args.length >= 2)) {
                            Util.sendMessage(player, "&cIncorrect format. You must specify a Discord username.");
                            return false;
                        }
                        String username = Util.getSentence(args, 1);
                        if (username.split("#").length != 2) {
                            Util.sendMessage(player, ChatColor.GRAY + username + "&c is not a valid discord username!\nDid you do &7\"Example#1234\"&c?");
                            return false;
                        }
                        User discordUser;
                        if (args.length >= 3) {
                            discordUser = Util.getDiscordUser(username);

                        } else {
                            discordUser = Util.getDiscordUser(username);
                        }
                        if (discordUser == null) {
                            Util.sendMessage(player, ChatColor.GRAY + username + "&c is not a valid discord username!\nDid you do &7\"Example#1234\"&c?");
                            return false;
                        }
                        String discordID = discordUser.getIdAsString();

                        try {
                            PreparedStatement ps = Heimdall.getNetwork().getConnection().prepareStatement(Network.GET_DISCORD_IDS_QUERY);
                            ps.setString(1, discordID);
                            ResultSet res = ps.executeQuery();
                            if (res.next()) {
                                Util.sendMessage(player, ChatColor.GRAY + username + "&c is already registered!");
                                return false;
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                            return false;
                        }
                        Util.sendMessage(player, ChatColor.GREEN + "Please check your Discord direct messages for the code.");
                        //send user message with discord bot
                        int code = new Random().nextInt(999999);
                        discordUser.sendMessage("Here is your 6 digit code: " + code);
                        discordUser.sendMessage("Please type /verify <code> in-game to be verified!");
                        Bukkit.getServer().broadcastMessage(discordUser.getId() + "");
                        userData.setLocalDiscordId("" + discordUser.getId());
                        Heimdall.pendingVerifications.put(userData, code);
                        return true;
                    }
                } else {
                    Util.sendMessage(player, "&cYou do not have permission to perform this command.");
                    return false;
                }
            }
            if(args.length == 0){
                Util.sendHelp(player);
                return false;
            }

            if (args[0].equals("create")) {
                if (player.hasPermission(Permissions.PERMISSION_CREATE)) {
                    if (Heimdall.ticketCreateCooldown.containsKey(player)) {
                        MyTimer timer = Heimdall.ticketCreateCooldown.get(player);
                        Util.sendMessage(player, "&c " + timer.timeLeft() + " until you can create another ticket!");
                        return false;
                    }
                    if (args.length >= 2) {
                        if (Heimdall.getUser(player) == null)
                            Heimdall.addUser(new UserData(player));
                        UserData userTicketData = Heimdall.getUser(player);
                        String reason = Util.getSentence(args, 1);
                        int ticketID = new Random().nextInt(99999);
                        userTicketData.createTicket(player, ticketID, reason, false, false);
                        MyTimer timer = new MyTimer(Bukkit.getServer().getPluginManager().getPlugin("Heimdall"), player);
                        timer.set(Heimdall.getConfigManager().TICKET_COOLDOWN * 60);
                        timer.start();
                        Heimdall.ticketCreateCooldown.put(player, timer);
                    } else {
                        Util.sendMessage(player, "&cYou must supply a reason in your ticket.");
                        return false;
                    }
                } else {
                    Util.sendMessage(player, "&cYou do not have permission to perform this command.");
                    return false;
                }
                //the list command
            } else if (args[0].equals("list")) {
                if (player.hasPermission(Permissions.PERMISSION_LIST)) {
                    if (Heimdall.getUser(player) == null)
                        Heimdall.addUser(new UserData(player));
                    UserData userTicketData = Heimdall.getUser(player);
                    userTicketData.regenerateTickets();
                    userTicketData.listTickets();
                    return true;
                } else {
                    Util.sendMessage(player, "&cYou do not have permission to perform this command.");
                    return false;
                }
            } else if (args[0].equals("close")) {
                if (player.hasPermission(Permissions.PERMISSION_CLOSE)) {
                    if (Util.checkTickets(player)) {
                        if (args.length >= 3) {
                            UserData userTicketData = Heimdall.getUser(player);
                            Ticket ticket;
                            try {
                                ticket = userTicketData.getTicket(Integer.parseInt(args[1]));
                            } catch (NumberFormatException e) {
                                Util.sendMessage(player, "&cThat is not a valid ticket#, if you do not know yours, use &7/ticket list&c.");
                                return false;
                            }
                            if (ticket == null) {
                                Util.sendMessage(player, "&cThat is not a valid ticket#, if you do not know yours, use &7/ticket list&c.");
                                return false;
                            }
                            String reason = Util.getSentence(args, 2);
                            ticket.markClosed(reason);
                            Util.sendMessage(player, "&aTicket#: &7" + ticket.getTicketID() + "&a has been marked as closed.");
                            return true;
                        } else {
                            Util.sendMessage(player, "&cPlease supply a ticket#, and reason. /ticket close");
                        }

                    } else {
                        Util.sendMessage(player, "&cYou have no active tickets. Use \"/ticket create\" to get started.");
                        return false;
                    }
                } else {
                    Util.sendMessage(player, "&cYou do not have permission to perform this command.");
                    return false;
                }
            } else {
                if (args.length < 1) {
                    Util.sendHelp(player);
                    return false;
                }
            }
            return false;
        }else{
            Util.sendMessage(player, "&cYou don't have permission to run this command.");
            return false;
        }
    }
}
