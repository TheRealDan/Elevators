package me.therealdan.e;

import me.therealdan.e.events.InteractHandler;
import me.therealdan.e.events.InventoryHandler;
import me.therealdan.e.mechanics.EditElevator;
import me.therealdan.e.mechanics.ElevatorHandler;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

/**
 * Created by Daniel on 13/01/2017.
 */
public class Elevators extends JavaPlugin {

    // Classes
    public EditElevator editElevator = new EditElevator();
    public ElevatorHandler elevatorHandler = new ElevatorHandler();

    // Configuration
    public FileConfiguration ElevatorData, Config = getConfig();
    public File ElevatorDataFile = new File(getDataFolder(), "ElevatorData.yml");

    public String color, color2;

    public void onEnable() {
        getServer().getPluginManager().registerEvents(new InteractHandler(this), this);
        getServer().getPluginManager().registerEvents(new InventoryHandler(this), this);

        saveDefaultConfig();
        ElevatorData = YamlConfiguration.loadConfiguration(ElevatorDataFile);
        saveElevatorData();

        color = ChatColor.translateAlternateColorCodes('&', Config.getString("Color.1"));
        color2 = ChatColor.translateAlternateColorCodes('&', Config.getString("Color.2"));

        getLogger().info("Custom plugin by TheRealDan");
    }

    public void saveElevatorData() {
        try {
            ElevatorData.save(ElevatorDataFile);
        } catch (Exception e) {
            //
        }
    }
}