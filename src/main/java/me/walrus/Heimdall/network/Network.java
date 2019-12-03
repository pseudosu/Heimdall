package me.walrus.Heimdall.network;

import me.walrus.Heimdall.Heimdall;
import org.bukkit.plugin.Plugin;

import java.sql.*;
import java.util.logging.Level;

public class Network {
    private static Connection c;
    private static String DATABASE;
    private static String USERNAME;
    private static String PASSWORD;
    private static String USERDATA_TABLE;
    private static String TICKET_DATA_TABLE;
    public static String USER_TICKET_DATA_QUERY;
    public static String CLOSE_TICKET_UPDATE_QUERY;
    public static String SOLVE_TICKET_UPDATE_QUERY;
    public static String CREATE_USER_DATA_QUERY;
    public static String GET_USER_DATA_QUERY;
    public static String CREATE_TICKET_QUERY;
    public static String VERIFY_USER_QUERY;
    public static String SET_DISCORD_ID_QUERY;
    public static String TICKET_DATA_FROM_ID_QUERY;
    public static String GET_DISCORD_IDS_QUERY;

    private String url;
    private Plugin p;

    public Network(Plugin p) {
        DATABASE = Heimdall.getConfigManager().getMYSQL_DATABASE();
        USERNAME = Heimdall.getConfigManager().getMYSQL_USERNAME();
        PASSWORD = Heimdall.getConfigManager().getMYSQL_PASSWORD();
        this.p = p;
        USERDATA_TABLE = Heimdall.getConfigManager().getUSERDATA_MYSQL_TABLE();
        TICKET_DATA_TABLE = Heimdall.getConfigManager().getTICKETDATA_MYSQL_TABLE();
        url = "jdbc:mysql://" + Heimdall.getConfigManager().getMYSQL_HOST() + ":3306/";
    }

    public void init() {
        USER_TICKET_DATA_QUERY = "SELECT * FROM " + TICKET_DATA_TABLE + " WHERE uuid = ?";
        CLOSE_TICKET_UPDATE_QUERY = "UPDATE " + TICKET_DATA_TABLE + " SET is_closed = ? WHERE ticket_id = ?";
        SOLVE_TICKET_UPDATE_QUERY = "UPDATE" + TICKET_DATA_TABLE + " SET is_solved = ? WHERE ticket_id = ?";
        CREATE_USER_DATA_QUERY = "INSERT INTO " + USERDATA_TABLE + " SET uuid = ?, discord_id = ?, is_verified = ?";
        CREATE_TICKET_QUERY = "INSERT INTO " + TICKET_DATA_TABLE + " SET uuid = ?, ticket_id = ?, issue = ?, is_solved = ?, is_closed = ?";
        GET_USER_DATA_QUERY = "SELECT * FROM " + USERDATA_TABLE + " WHERE uuid = ?";
        VERIFY_USER_QUERY = "UPDATE " + USERDATA_TABLE + " SET is_verified = ? WHERE uuid = ?";
        SET_DISCORD_ID_QUERY = "UPDATE " + USERDATA_TABLE + " SET discord_id = ? WHERE uuid = ?";
        TICKET_DATA_FROM_ID_QUERY = "SELECT * FROM " + TICKET_DATA_TABLE + " WHERE ticket_id = ?";
        GET_DISCORD_IDS_QUERY = "SELECT * FROM " + USERDATA_TABLE + " WHERE discord_id = ?";

        try {
            Statement s = c.createStatement();
            boolean success = false;
            String usertablequery = "CREATE TABLE IF NOT EXISTS "
                    + USERDATA_TABLE +
                    " (id int NOT NULL AUTO_INCREMENT," +
                    " uuid VARCHAR(255)," +
                    " discord_id VARCHAR(40)," +
                    " is_verified boolean," +
                    " PRIMARY KEY (id))";
            String tickettablequery = "CREATE TABLE IF NOT EXISTS " +
                    TICKET_DATA_TABLE +
                    " (id INT NOT NULL AUTO_INCREMENT," +
                    " uuid VARCHAR(255)," +
                    " ticket_id VARCHAR(255)," +
                    " issue VARCHAR(255)," +
                    " is_solved boolean," +
                    " is_closed boolean," +
                    " PRIMARY KEY (id))";
            int i = s.executeUpdate(usertablequery);
            if (i > 0) {
                p.getLogger().log(Level.INFO, "Created table '"
                        + TICKET_DATA_TABLE +
                        "' with the fields: id int," +
                        " username VARCHAR(40)," +
                        " active_tickets VARCHAR(255)," +
                        " discord_id VARCHAR(40)," +
                        " is_verified boolean");
            } else {
                success = true;
            }
            int z = s.executeUpdate(tickettablequery);
            if (z > 0) {
                p.getLogger().log(Level.INFO, "Created table '"
                        + TICKET_DATA_TABLE +
                        "' with the fields: id int," +
                        " username VARCHAR(40)," +
                        " ticket_id VARCHAR(255)," +
                        " is_solved boolean");
            } else {
                success = true;
            }
            if (success) {
                p.getLogger().log(Level.INFO, "Database is ready, let's rock.");
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    public boolean connect() {
        try {
            c = DriverManager.getConnection(url + DATABASE, USERNAME, PASSWORD);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public ResultSet executeQuery(String query) {
        try {
            PreparedStatement ps = c.prepareStatement(query);
            ResultSet res = ps.executeQuery();
            if (res.next())
                return res;
            else
                return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean executeUpdate(String query) {
        try {
            PreparedStatement ps = c.prepareStatement(query);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println(query);
            return false;

        }
    }

    public Connection getConnection() {
        return c;
    }

}
