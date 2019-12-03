package me.walrus.Heimdall.commands;

import me.walrus.Heimdall.Heimdall;
import me.walrus.Heimdall.network.Network;
import me.walrus.Heimdall.network.Ticket;
import me.walrus.Heimdall.network.UserData;
import me.walrus.Heimdall.util.Util;
import org.javacord.api.entity.channel.ChannelCategory;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DiscordCmdTicket implements MessageCreateListener {
    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        if (event.getMessageAuthor().isYourself())
            return;
        String message = event.getMessageContent();
        if (message.startsWith("-close")) {
            ServerTextChannel textChannel = event.getServerTextChannel().get();
            ChannelCategory category = textChannel.getCategory().get();
            if (!category.getIdAsString().equals(Heimdall.getConfigManager().BOT_CATEGORY)) {
                textChannel.sendMessage("This command can only be used within a ticket!");
                return;
            }

            int channel = Integer.parseInt(textChannel.getName().replace("ticket-", ""));
            String reasonForClosure;
            String realMessage = message.replace("-close", "");
            String[] args = realMessage.split(" ");
            if (args.length <= 1) {
                textChannel.sendMessage("Please supply a reason for closing this ticket!");
                return;
            }
            reasonForClosure = Util.getSentence(args, 0);
            Ticket currentTicket = Heimdall.getTicket(channel);
            if (currentTicket == null)
                return;
            UserData userData = Heimdall.getUserFromTicketID(currentTicket.getTicketID());
            if (userData == null) {
                textChannel.sendMessage("I wasn't able to get the users data from the database. Please report this to the developer.");
                return;
            }
            userData.generateMySQLData();
            try {
                PreparedStatement ps = Heimdall.getNetwork().getConnection().prepareStatement(Network.GET_USER_DATA_QUERY);
                ps.setString(1, userData.getUUID().toString());
                ResultSet res = ps.executeQuery();
                if (res.next()) {
                    userData.setLocalDiscordId(res.getString("discord_id"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            User sender = event.getMessageAuthor().asUser().get();
            Role staffRole = event.getServer().get().getRolesByName(Heimdall.getConfigManager().BOT_STAFF_ROLE).get(0);
            boolean isStaff = sender.getRoles(event.getServer().get()).contains(staffRole);
            currentTicket.markClosedDiscord(reasonForClosure, sender.getName(), isStaff);
        }
    }
}
