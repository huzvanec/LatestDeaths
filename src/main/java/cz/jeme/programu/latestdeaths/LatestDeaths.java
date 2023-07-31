package cz.jeme.programu.latestdeaths;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.logging.Level;

public class LatestDeaths extends JavaPlugin {

    private Database database;
    public static FileConfiguration config;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reload();
        DeathUtils.loadDatePattern();

        try {
            database = new Database(getDataFolder());
        } catch (ClassNotFoundException | SQLException e) {
            serverLog(Level.SEVERE, "Couldn't establish connection with database!");
            e.printStackTrace();
        }

        new LastDeathsCommand(database);

        PluginManager pluginManager = Bukkit.getServer().getPluginManager();
        pluginManager.registerEvents(new EventListener(database), this);
    }

    @Override
    public void onDisable() {
        try {
            database.closeConnection();
        } catch (SQLException e) {
            serverLog(Level.SEVERE, "Unable to close connection!");
            e.printStackTrace();
        }
    }

    public static void reload() {
        config = LatestDeaths.getPlugin(LatestDeaths.class).getConfig();
    }

    public static void serverLog(Level lvl, String msg) {
        if (msg == null) {
            throw new NullPointerException("Message is null!");
        }
        Bukkit.getServer().getLogger().log(lvl, Messages.strip(Messages.PREFIX) + msg);
    }
}