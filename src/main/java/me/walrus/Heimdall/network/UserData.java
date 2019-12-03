package me.walrus.Heimdall.network;

import me.walrus.Heimdall.Heimdall;
import me.walrus.Heimdall.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;

import java.awt.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.bukkit.Bukkit.getServer;

public class UserData {
    private UUID uuid;
    private OfflinePlayer player;
    private ArrayList<Ticket> activeTickets;
    private ResultSet userMySQLData;
    private String discord_id;
    private boolean isVerified;
    private User discordUser;
    private boolean isOffline;

    public UserData(OfflinePlayer player) {
        if (player.getPlayer() == null) {
            isOffline = true;
            this.player = player;
        } else {
            this.player = player;
            isOffline = false;
        }
        this.uuid = player.getUniqueId();
        activeTickets = loadTickets();
        if (isVerified) {
            try {
                discordUser = Heimdall.getDiscord().getUserById(discord_id).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    public UUID getUUID() {
        return uuid;
    }

    public String getUsername() {
        return player.getName();
    }

    public boolean isVerified() {
        return isVerified;
    }

    public boolean isOffline() {
        return isOffline;
    }

    public String getDiscordId() {
        return discord_id;
    }

    public User getLocalDiscordUser() {
        return discordUser;
    }
    public void setMySQLDiscordID(String id) {
        try {
            PreparedStatement preparedStatement = Heimdall.getNetwork().getConnection().prepareStatement(Network.SET_DISCORD_ID_QUERY);
            preparedStatement.setString(1, id);
            preparedStatement.setString(2, getUUID().toString());
            preparedStatement.executeUpdate();
            discord_id = id;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setLocalDiscordId(String id) {
        discord_id = id;
        try {
            discordUser = Heimdall.getDiscord().getUserById(id).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void generateMySQLData() {
        try {
            PreparedStatement userdataStatement = Heimdall.getNetwork().getConnection().prepareStatement(Network.GET_USER_DATA_QUERY);
            userdataStatement.setString(1, getUUID().toString());
            userMySQLData = userdataStatement.executeQuery();
            userMySQLData.next();
            generateLocalData();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void regenerateTickets() {
        activeTickets = loadTickets();
    }

    private void generateLocalData() throws SQLException {
        discord_id = userMySQLData.getString("discord_id");
        isVerified = userMySQLData.getBoolean("is_verified");
        if (!discord_id.equals("null")) {
            discordUser = Util.getDiscorduserByID(getDiscordId());
        } else {
            discordUser = null;
        }
        loadTickets();
    }

    public ArrayList<Ticket> getActiveTickets() {
        return activeTickets;
    }

    public Ticket getTicket(int ticketID) {
        for (Ticket ticket : getActiveTickets()) {
            if (ticket.getTicketID() == ticketID)
                return ticket;
        }
        return null;
    }

    public void createTicket(Player player, int ticketID, String reason, boolean isSolved, boolean isClosed) {
        Ticket ticket = new Ticket(player, ticketID, reason, isSolved, isClosed);
        BukkitScheduler scheduler = getServer().getScheduler();
        scheduler.runTaskAsynchronously(Bukkit.getPluginManager().getPlugin("Heimdall"), () -> {
            try {
                PreparedStatement preparedStatement = Heimdall.getNetwork().getConnection().prepareStatement(Network.CREATE_TICKET_QUERY);
                preparedStatement.setString(1, getUUID().toString());
                preparedStatement.setInt(2, ticket.getTicketID());
                preparedStatement.setString(3, ticket.getIssue());
                preparedStatement.setBoolean(4, ticket.isSolved());
                preparedStatement.setBoolean(5, ticket.isClosed());
                preparedStatement.executeUpdate();
                getActiveTickets().add(ticket);

                ticket.createDiscordChannel();
                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .addField("Player", player.getName())
                        .addField("Nickname", ChatColor.stripColor(player.getDisplayName()))
                        .addField("Discord Name", getLocalDiscordUser().getDiscriminatedName())
                        .addField("Opened ticket", "Ticket# " + ticket.getTicketID())
                        .addField("Reason", ticket.getIssue())
                        .setColor(Color.orange);
                Util.sendEmbed(embedBuilder, Heimdall.getConfigManager().BOT_TICKET_LOG_CHANNEL_ID);

                Util.sendMessage(player, "&aTicket Created. Your ticket ID is " + ticketID);
                Util.sendMessage(player, "&aYou now have access to the channel ticket-" + ticketID + " on Discord.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void listTickets() {
        if (Util.checkTickets(player.getPlayer())) {
            Util.sendMessage(player.getPlayer(), "&aHere are your active tickets.");
            for (Ticket ticket : getActiveTickets()) {
                Util.sendMessage(player.getPlayer(), "&aTicket #&7: " + ticket.getTicketID());
                Util.sendMessage(player.getPlayer(), "&aReason&7: " + ticket.getIssue());
                Util.sendMessage(player.getPlayer(), "Solved: " + ticket.isSolved() + ", Closed: " + ticket.isClosed());
            }
        } else {
            Util.sendMessage(player.getPlayer(), "&cYou have no active tickets. Use \"/ticket create\" to get started.");
        }
    }

    private ArrayList<Ticket> loadTickets() {
        ArrayList<Ticket> tickets = new ArrayList<>();

        try {
            PreparedStatement preparedStatement = Heimdall.getNetwork().getConnection().prepareStatement(Network.USER_TICKET_DATA_QUERY);
            preparedStatement.setString(1, uuid.toString());
            ResultSet res = preparedStatement.executeQuery();
            while (res.next()) {
                int ticketID = res.getInt("ticket_id");
                String issue = res.getString("issue");
                boolean isSolved = res.getBoolean("is_solved");
                boolean isClosed = res.getBoolean("is_closed");

                if (!isClosed && !isSolved) {
                    tickets.add(new Ticket(player, ticketID, issue, isSolved, isClosed));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tickets;
    }

    public void setVerified(boolean b) {
        try {
            PreparedStatement preparedStatement = Heimdall.getNetwork().getConnection().prepareStatement(Network.VERIFY_USER_QUERY);
            preparedStatement.setBoolean(1, b);
            preparedStatement.setString(2, getUUID().toString());
            preparedStatement.executeUpdate();
            isVerified = b;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
