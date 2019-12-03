package me.walrus.Heimdall.util;

import org.bukkit.plugin.Plugin;

import java.util.ArrayList;

public class Config {

    Plugin p;
    public final String CONFIG_PATH = "configuration.settings.";
    public String PREFIX;
    public String BOT_TOKEN;
    public String BOT_SERVER_ID;
    public String BOT_STAFF_CHAT;
    public String BOT_TICKET_LOG_CHANNEL_ID;
    public String BOT_STAFF_ROLE;
    public String BOT_CATEGORY;
    public String BOT_ACTIVITY;
    public String BOT_COMMAND_LOG_CHANNEL_ID;
    public String STAFF_CHAT_FORMAT;
    public String STAFF_CHAT_DISCORD_FORMAT;
    public String STAFF_CHAT_TO_DISCORD_FORMAT;
    public int TICKET_COOLDOWN;
    public static ArrayList<String> heimdallQuotes = new ArrayList<>();


    public Config(Plugin p) {
        this.p = p;
    }

    public void init() {
        p.getConfig().options().copyDefaults(true);
        p.getConfig().options().header("Strings that can be used for formatting: %playername%, %playernickname%, %message%");
        p.getConfig().addDefault(CONFIG_PATH + "plugin.prefix", "&8[&6Heimdall&8] ");
        p.getConfig().addDefault(CONFIG_PATH + "plugin.ticketcooldown", 5);
        p.getConfig().addDefault(CONFIG_PATH + "plugin.format.staffchat", "&7[&cStaffChat&7] &c%player%&7: &6%message%");
        p.getConfig().addDefault(CONFIG_PATH + "plugin.format.staffchat_fromdiscord", "&7[&9DISCORDSTAFF&7] &c%playername%&7: &6%message%");
        p.getConfig().addDefault(CONFIG_PATH + "plugin.format.todiscord", "[StaffChat] %playernickname%: %message%");
        p.getConfig().addDefault(CONFIG_PATH + "plugin.permissions.base", "ticket.base");
        p.getConfig().addDefault(CONFIG_PATH + "plugin.permissions.verify", "ticket.verify");
        p.getConfig().addDefault(CONFIG_PATH + "plugin.permissions.create", "ticket.create");
        p.getConfig().addDefault(CONFIG_PATH + "plugin.permissions.list", "ticket.list");
        p.getConfig().addDefault(CONFIG_PATH + "plugin.permissions.close", "ticket.close");
        p.getConfig().addDefault(CONFIG_PATH + "plugin.permissions.admin", "ticket.admin");
        p.getConfig().addDefault(CONFIG_PATH + "plugin.permissions.isstaff", "ticket.admin.isstaff");
        p.getConfig().addDefault(CONFIG_PATH + "plugin.permissions.staffchat", "ticket.admin.staffchat");
        p.getConfig().addDefault(CONFIG_PATH + "plugin.permissions.logcommands", "ticket.logcommands");
        p.getConfig().addDefault(CONFIG_PATH + "plugin.permissions.logcommandsexempt", "ticket.logcommandsexempt");
        p.getConfig().addDefault(CONFIG_PATH + "plugin.permissions.commandspy", "ticket.commandspy");
        p.getConfig().addDefault(CONFIG_PATH + "plugin.permissions.commandspyexempt", "ticket.commandspyexempt");

        p.getConfig().addDefault(CONFIG_PATH + "bot.token", "your token here");
        p.getConfig().addDefault(CONFIG_PATH + "bot.server_id", "the id of your server");
        p.getConfig().addDefault(CONFIG_PATH + "bot.staff_chat_channel", "the id of the channel that the staffchat command shall use.");
        p.getConfig().addDefault(CONFIG_PATH + "bot.log_channel", "the channel id this bot should use for updates");
        p.getConfig().addDefault(CONFIG_PATH + "bot.staff_role", "the role name the bot should use to send messages out.");
        p.getConfig().addDefault(CONFIG_PATH + "bot.category_id", "the category id this bot should place ticket channels under");
        p.getConfig().addDefault(CONFIG_PATH + "bot.command_log_channel", "the id of the channel this bot should log commands in");
        p.getConfig().addDefault(CONFIG_PATH + "bot.activity", "being worked on");

        p.getConfig().addDefault(CONFIG_PATH + "mysql.host", "localhost");
        p.getConfig().addDefault(CONFIG_PATH + "mysql.database", "utopiacontrol");
        p.getConfig().addDefault(CONFIG_PATH + "mysql.username", "root");
        p.getConfig().addDefault(CONFIG_PATH + "mysql.password", "");
        p.getConfig().addDefault(CONFIG_PATH + "mysql.userdatatable", "player_data");
        p.getConfig().addDefault(CONFIG_PATH + "mysql.ticketdatatable", "ticket_data");
        p.saveConfig();
        setLocalConstants();

        heimdallQuotes.add("Be warned, I shall uphold my sacred oath to protect this realm as its gatekeeper. If your return threatens the safety of Asgard, my gate will remain shut and you will be left to perish on the cold waste of Jotunheim.");
        heimdallQuotes.add("They shall not pass.");
        heimdallQuotes.add("Even blinded, I can see the truth");
        heimdallQuotes.add("Merciful and just are the laws of Asgard, but woe to he who strays from the path of righteousness!");
        heimdallQuotes.add("The very act of my peering forward through time \"locks\" what I see into an absolute certainty...");
    }

    private void setLocalConstants() {
        BOT_TOKEN = getString("bot.token");
        BOT_SERVER_ID = getString("bot.server_id");
        BOT_STAFF_CHAT = getString("bot.staff_chat_channel");
        BOT_TICKET_LOG_CHANNEL_ID = getString("bot.log_channel");
        BOT_STAFF_ROLE = getString("bot.staff_role");
        BOT_CATEGORY = getString("bot.category_id");
        BOT_ACTIVITY = getString("bot.activity");
        BOT_COMMAND_LOG_CHANNEL_ID = getString("bot.command_log_channel");
        STAFF_CHAT_FORMAT = getString("plugin.format.staffchat");
        STAFF_CHAT_DISCORD_FORMAT = getString("plugin.format.staffchat_fromdiscord");
        STAFF_CHAT_TO_DISCORD_FORMAT = getString("plugin.format.todiscord");
        PREFIX = getString("plugin.prefix");
        TICKET_COOLDOWN = p.getConfig().getInt(CONFIG_PATH + "plugin.ticketcooldown");

    }

    public String getString(String path) {
        return p.getConfig().getString(CONFIG_PATH + path);
    }

    public String getMYSQL_HOST() {
        return getString("mysql.host");
    }

    public String getMYSQL_DATABASE() {
        return getString("mysql.database");
    }

    public String getMYSQL_USERNAME() {
        return getString("mysql.username");
    }

    public String getMYSQL_PASSWORD() {
        return getString("mysql.password");
    }

    public String getUSERDATA_MYSQL_TABLE() {
        return getString("mysql.userdatatable");
    }

    public String getTICKETDATA_MYSQL_TABLE() {
        return getString("mysql.ticketdatatable");
    }
}
