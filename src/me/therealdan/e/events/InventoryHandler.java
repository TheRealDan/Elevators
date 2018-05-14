package me.therealdan.e.events;

import me.therealdan.e.Elevators;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

/**
 * Created by Daniel on 13/01/2017.
 */
public class InventoryHandler implements Listener {

    public Elevators m;

    public InventoryHandler(Elevators elevators) {
        m = elevators;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        m.elevatorHandler.onClick(m, event);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        m.elevatorHandler.onClose(m, event);
    }
}