package me.therealdan.e.events;

import me.therealdan.e.Elevators;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Created by Daniel on 13/01/2017.
 */
public class InteractHandler implements Listener {

    public Elevators m;

    public InteractHandler(Elevators elevators) {
        // temp 2
        m = elevators;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        m.elevatorHandler.onInteract(m, event);
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        m.elevatorHandler.onPlace(m, event);
    }
}