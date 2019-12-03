package me.walrus.Heimdall.util;

import me.walrus.Heimdall.Heimdall;

public class Permissions {
    public static final String PERMISSION_BASE = Heimdall.getConfigManager().getString("plugin.permissions.base");
    public static final String PERMISSION_VERIFY = Heimdall.getConfigManager().getString("plugin.permissions.verify");
    public static final String PERMISSION_CREATE = Heimdall.getConfigManager().getString("plugin.permissions.create");
    public static final String PERMISSION_LIST = Heimdall.getConfigManager().getString("plugin.permissions.list");
    public static final String PERMISSION_CLOSE = Heimdall.getConfigManager().getString("plugin.permissions.close");
    public static final String PERMISSION_ADMIN = Heimdall.getConfigManager().getString("plugin.permissions.admin");
    public static final String PERMISSION_ISSTAFF = Heimdall.getConfigManager().getString("plugin.permissions.isstaff");
    public static final String PERMISSION_STAFFCHAT = Heimdall.getConfigManager().getString("plugin.permissions.staffchat");
    public static final String PERMISSION_LOGCOMMANDS = Heimdall.getConfigManager().getString("plugin.permissions.logcommands");
    public static final String PERMISSION_LOGCOMMANDSEXEMPT = Heimdall.getConfigManager().getString("plugin.permissions.logcommandsexempt");
    public static final String PERMISSION_COMMANDSPY = Heimdall.getConfigManager().getString("plugin.permissions.commandspy");
    public static final String PERMISSION_COMMANDSPYEXEMPT = Heimdall.getConfigManager().getString("plugin.permissions.commandspyexempt");
}
