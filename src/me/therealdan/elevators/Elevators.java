package me.therealdan.elevators;

import me.therealdan.elevators.mechanics.Elevator;
import me.therealdan.elevators.mechanics.ElevatorHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

public class Elevators extends JavaPlugin {

    private static Elevators elevators;
    public static String MAIN, SECOND;

    private ElevatorHandler elevatorHandler;

    public void onEnable() {
        elevators = this;

        saveDefaultConfig();

        MAIN = ChatColor.translateAlternateColorCodes('&', getConfig().getString("Color.Main"));
        SECOND = ChatColor.translateAlternateColorCodes('&', getConfig().getString("Color.Secondary"));

        Elevator.load();

        getServer().getPluginManager().registerEvents(getElevatorHandler(), this);
    }

    @Override
    public void onDisable() {
        Elevator.unload();
    }

    public ElevatorHandler getElevatorHandler() {
        if (elevatorHandler == null) elevatorHandler = new ElevatorHandler();
        return elevatorHandler;
    }

    public static void play(Location location, String sound, float volume, float pitch) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "playsound " + sound.toLowerCase() + " master @a " + location.getBlockX() + " " + location.getBlockY() + " " + location.getBlockZ() + " " + volume + " " + pitch);
    }

    public static Elevators getInstance() {
        return elevators;
    }
}