package me.therealdan;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created by Daniel on 13/01/2017.
 */
public class Elevators extends JavaPlugin {

    // TODO Test

    public static String MAIN, SECOND;

    public void onEnable() {
        saveDefaultConfig();

        MAIN = ChatColor.translateAlternateColorCodes('&', getConfig().getString("Color.Main"));
        SECOND = ChatColor.translateAlternateColorCodes('&', getConfig().getString("Color.Secondary"));
    }
}