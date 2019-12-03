package me.walrus.Heimdall.network;

import me.walrus.Heimdall.Heimdall;
import me.walrus.Heimdall.util.Util;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.javacord.api.entity.channel.ChannelCategory;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.*;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

import java.awt.*;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;

public class Ticket {

    private OfflinePlayer owner;
    private int ticketID;
    private String issue;
    private boolean isSolved;
    private boolean isClosed;
    private Location loc;

    /**
     * @param owner    the owner of the ticket
     * @param ticketID the id of the ticket
     * @param issue    the issue
     * @param isSolved is the ticket solved
     * @param isClosed has the ticket been closed
     */
    public Ticket(OfflinePlayer owner, int ticketID, String issue, boolean isSolved, boolean isClosed) {
        this.owner = owner;
        this.ticketID = ticketID;
        this.issue = issue;
        this.isSolved = isSolved;
        this.isClosed = isClosed;
    }

    public OfflinePlayer getOwner() {
        return owner;
    }

    public int getTicketID() {
        return ticketID;
    }

    String getIssue() {
        return issue;
    }

    boolean isSolved() {
        return isSolved;
    }

    boolean isClosed() {
        return isClosed;
    }

    void createDiscordChannel() {
        long serverID = Long.parseLong(Heimdall.getConfigManager().getString("bot.server_id"));
        long categoryID = Long.parseLong(Heimdall.getConfigManager().getString("bot.category_id"));
        UserData user = Heimdall.getUser(owner);
        User discordUser;

        Server server = Heimdall.getDiscord().getServerById(serverID).get();
        Permissions permissionsClient = new PermissionsBuilder()
                .setState(PermissionType.READ_MESSAGES, PermissionState.ALLOWED)
                .setState(PermissionType.SEND_MESSAGES, PermissionState.ALLOWED).build();
        Permissions permissionsEveryone = new PermissionsBuilder()
                .setState(PermissionType.READ_MESSAGES, PermissionState.DENIED)
                .setState(PermissionType.SEND_MESSAGES, PermissionState.DENIED).build();
        Role role;
        Role everyone = everyone = server.getRolesByName("@everyone").get(0);
        ChannelCategory category = server.getChannelCategoryById(categoryID).get();
        try {
            role = server.createRoleBuilder().setName("ticket-" + getTicketID()).create().get();
            discordUser = Heimdall.getDiscord().getUserById(user.getDiscordId()).get();
            discordUser.addRole(role);
        } catch (InterruptedException e) {
            role = null;
            e.printStackTrace();
        } catch (ExecutionException e) {
            role = null;
            e.printStackTrace();
        }
        TextChannel textChannel;
        try {
            textChannel = server.createTextChannelBuilder()
                    .setName("ticket-" + getTicketID())
                    .setCategory(category)
                    .addPermissionOverwrite(role, permissionsClient)
                    .addPermissionOverwrite(everyone, permissionsEveryone)
                    .setTopic("Issue: " + getIssue()).create().get();
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Ticket #: " + getTicketID() + ", issue: " + getIssue())
                    .setDescription("Your ticket has been created. A staff member will be with you shortly to assist.")
                    .addField("-ticket close", "Use this to close the ticket.")
                    .setColor(Color.ORANGE);
            textChannel = server.getTextChannelsByName("ticket-" + getTicketID()).get(0);
            textChannel.sendMessage(embed);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

    }

    public void deleteDiscordChannel(String name) {
        long serverID = Long.parseLong(Heimdall.getConfigManager().getString("bot.server_id"));
        Server server = Heimdall.getDiscord().getServerById(serverID).get();
        server.getTextChannelsByName(name).get(0).delete();
    }

    public void deleteDiscordRole() {
        long serverID = Long.parseLong(Heimdall.getConfigManager().getString("bot.server_id"));
        Server server = Heimdall.getDiscord().getServerById(serverID).get();
        UserData user = Heimdall.getUserFromTicketID(ticketID);
        user.generateMySQLData();
        Role role = server.getRolesByName("ticket-" + getTicketID()).get(0);
        user.getLocalDiscordUser().removeRole(role);
    }

    public void markSolved() {
        isSolved = true;
        PreparedStatement preparedStatement;
        try {
            preparedStatement = Heimdall.getNetwork().getConnection().prepareStatement(Network.SOLVE_TICKET_UPDATE_QUERY);
            preparedStatement.setBoolean(1, isSolved);
            preparedStatement.setString(2, owner.getUniqueId().toString());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void markClosed(String closeReason) {
        isClosed = true;
        PreparedStatement preparedStatement;
        try {
            preparedStatement = Heimdall.getNetwork().getConnection().prepareStatement(Network.CLOSE_TICKET_UPDATE_QUERY);
            preparedStatement.setBoolean(1, isClosed);
            preparedStatement.setInt(2, ticketID);
            preparedStatement.executeUpdate();

            UserData ownerData = Heimdall.getUser(owner);
            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .addField("Player", owner.getName())
                    .addField("Discord Name", ownerData.getLocalDiscordUser().getDiscriminatedName())
                    .addField("Closed ticket", "Ticket# " + getTicketID())
                    .addField("Reason", closeReason)
                    .setColor(Color.ORANGE);
            Util.sendEmbed(embedBuilder, Heimdall.getConfigManager().BOT_TICKET_LOG_CHANNEL_ID);
            ownerData.getActiveTickets().remove(this);

            deleteDiscordChannel("ticket-" + getTicketID());
            deleteDiscordRole();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void markClosedDiscord(String reason, String whoClosed, boolean isStaff) {
        UserData ownerData = Heimdall.getUserFromTicketID(getTicketID());
        if (ownerData != null) {
            if (!ownerData.isOffline())
                ownerData = Heimdall.getUser(owner);
            ownerData.generateMySQLData();
            EmbedBuilder embedBuilder;
            if (isStaff) {
                embedBuilder = new EmbedBuilder()
                        .addField("Staff Member", whoClosed)
                        .addField("Closed ticket", "Ticket# " + getTicketID())
                        .addField("Reason", reason)
                        .setColor(Color.ORANGE);
                ownerData.getLocalDiscordUser().sendMessage("Ticket#: " + getTicketID() + " has beeen solved by staff member: " + whoClosed + "\nReason: " + reason);
                if (!ownerData.isOffline()) {
                    Util.sendMessage(owner.getPlayer(), "&aTicket#: &7" + getTicketID());
                    Util.sendMessage(owner.getPlayer(), "&aHas been solved by staff member:&7 " + whoClosed);
                    Util.sendMessage(owner.getPlayer(), "&aReason: &7" + reason);
                    ownerData.getActiveTickets().remove(this);
                } else {
                    Heimdall.notificationsToSend.put(owner, new String[]{
                            "&aTicket#: &7" + getTicketID(),
                            "&aHas been solved by staff member:&7 " + whoClosed,
                            "&aReason: &7" + reason});
                }
            } else {
                embedBuilder = new EmbedBuilder()
                        .addField("Player", owner.getName())
                        .addField("Discord Name", ownerData.getLocalDiscordUser().getDiscriminatedName())
                        .addField("Closed ticket", "Ticket# " + getTicketID())
                        .addField("Reason", reason)
                        .setColor(Color.ORANGE);
            }
            Util.sendEmbed(embedBuilder, Heimdall.getConfigManager().BOT_TICKET_LOG_CHANNEL_ID);
            deleteDiscordRole();
            deleteDiscordChannel("ticket-" + getTicketID());

            isClosed = true;
            PreparedStatement preparedStatement;
            try {
                preparedStatement = Heimdall.getNetwork().getConnection().prepareStatement(Network.CLOSE_TICKET_UPDATE_QUERY);
                preparedStatement.setBoolean(1, isClosed);
                preparedStatement.setInt(2, ticketID);
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
