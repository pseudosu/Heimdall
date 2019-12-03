package me.walrus.Heimdall;

import me.walrus.Heimdall.commands.*;
import me.walrus.Heimdall.listeners.LoginListener;
import me.walrus.Heimdall.listeners.OnChat;
import me.walrus.Heimdall.listeners.OnChatDiscord;
import me.walrus.Heimdall.network.Network;
import me.walrus.Heimdall.network.Ticket;
import me.walrus.Heimdall.network.UserData;
import me.walrus.Heimdall.util.Config;
import me.walrus.Heimdall.util.MyTimer;
import me.walrus.Heimdall.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;

import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

public final class Heimdall extends JavaPlugin {

    private static Config config;
    private static DiscordApi api;
    private static Network network;
    private static ArrayList<UserData> users = new ArrayList<>();
    public static HashMap<OfflinePlayer, String[]> notificationsToSend = new HashMap<>();
    public static HashMap<UserData, Integer> pendingVerifications = new HashMap<>();
    public static ArrayList<Player> staffChatUsers = new ArrayList<>();
    public static HashMap<Player, Integer> ticketChatUsers = new HashMap<>();
    public static HashMap<Player, MyTimer> ticketCreateCooldown = new HashMap<>();

    public static void removeUser(Player player) {
        users.remove(getUser(player));
    }

    @Override
    public void onEnable() {
        PluginManager pm = Bukkit.getServer().getPluginManager();

        //Register commands and events
        getCommand("ticket").setExecutor(new CmdTicket());
        getCommand("verify").setExecutor(new CmdVerify());
        getCommand("staffchat").setExecutor(new CmdStaffChat());
        getCommand("hreload").setExecutor(new CmdReload(this));

        pm.registerEvents(new LoginListener(), this);
        pm.registerEvents(new OnChat(), this);

        //init config/network
        config = new Config(this);
        config.init();
        Bukkit.getConsoleSender().sendMessage("------------");
        Bukkit.getConsoleSender().sendMessage("Connecting to MySQL Database");
        network = new Network(this);

        if (network.connect()) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "Connected");
        } else {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Failed to connect to MySQL database. Check the configuration.");
        }
        network.init();

        Bukkit.getConsoleSender().sendMessage("Starting Discord Bot");
        startBot();
        Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "Bot started");
        Bukkit.getConsoleSender().sendMessage(ChatColor.BOLD + "" + ChatColor.BLUE + "We are ready to go");
        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + Util.getRandomHeimdallQuote());
        Bukkit.getConsoleSender().sendMessage("------------");
        getLogger().log(Level.INFO, "Heimdall started.");
        if(Bukkit.getOnlinePlayers().size() > 0){
            for(Player player : Bukkit.getOnlinePlayers()){
                addUser(new UserData(player));
            }
        }
    }
    public void onReload(){

    }

    @Override
    public void onDisable() {

        api.disconnect();
    }

    public static DiscordApi getDiscord() {
        return api;
    }

    private void startBot() {
        api = new DiscordApiBuilder().setToken(getConfigManager().BOT_TOKEN).login().join();
        api.updateActivity(getConfigManager().BOT_ACTIVITY);
        api.addListener(new DiscordCmdTicket());
        api.addListener(new OnChatDiscord());
        getLogger().log(Level.INFO, "Invite Heimdall with the following link:");
        getLogger().log(Level.INFO, api.createBotInvite());
    }

    public boolean reloadHeimdall() {
        reloadConfig();
        config = new Config(this);
        config.init();
        network = new Network(this);
        if (network.connect()) {
            network.init();
            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "Connected");
            return true;
        } else {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Failed to connect to MySQL database. Check the configuration.");
            return false;
        }
    }

    public static Config getConfigManager() {
        return config;
    }

    public static Network getNetwork() {
        return network;
    }

    public static UserData createUser(OfflinePlayer player) {
        for (UserData data : users) {
            if (data.getUsername().equals(player.getName())) {
                data.generateMySQLData();
                return data;
            }
        }
        return null;
    }

    public static UserData getUser(OfflinePlayer player) {
        for (UserData data : users) {
            if (data.getUsername().equals(player.getName())) {
                return data;
            }
        }
        return createUser(player);
    }

    public static UserData getUserFromTicketID(long ticketid) {
        String uuid;
        Ticket ticket = getTicket(ticketid);

        try {
            PreparedStatement preparedStatement = Heimdall.getNetwork().getConnection().prepareStatement(Network.TICKET_DATA_FROM_ID_QUERY);
            preparedStatement.setInt(1, (int) ticketid);
            ResultSet res = preparedStatement.executeQuery();
            res.next();
            uuid = res.getString("uuid");
            OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(uuid));
            UserData userData = new UserData(player);
            userData.generateMySQLData();

            return userData;
        } catch (SQLException e) {
            return null;
        }
    }

    public static Ticket getTicket(long ticketID) {
        try {
            PreparedStatement preparedStatement = Heimdall.getNetwork().getConnection().prepareStatement(Network.TICKET_DATA_FROM_ID_QUERY);
            preparedStatement.setLong(1, ticketID);
            ResultSet res = preparedStatement.executeQuery();
            res.next();
            String uuid = res.getString("uuid");
            String issue = res.getString("issue");
            boolean isSolved = res.getBoolean("is_solved");
            boolean isClosed = res.getBoolean("is_closed");
            OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(uuid));
            return new Ticket(player, (int) ticketID, issue, isSolved, isClosed);
        } catch (SQLException e) {
        }
        return null;
    }

    public static void addUser(UserData userData) {
        try {
            PreparedStatement userdataStatement = getNetwork().getConnection().prepareStatement(Network.GET_USER_DATA_QUERY);
            userdataStatement.setString(1, userData.getUUID().toString());
            if (!userdataStatement.executeQuery().next()) {
                PreparedStatement createUserDataStatement = getNetwork().getConnection().prepareStatement(Network.CREATE_USER_DATA_QUERY);
                createUserDataStatement.setString(1, userData.getUUID().toString());
                createUserDataStatement.setString(2, "null");
                createUserDataStatement.setBoolean(3, false);
                createUserDataStatement.executeUpdate();
            } else {
                userData.generateMySQLData();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (!users.contains(userData))
            users.add(userData);
    }
}
