package me.therealdan.elevators.mechanics;

import me.therealdan.elevators.Elevators;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class ElevatorHandler implements Listener {

    private HashMap<UUID, Elevator> elevatorPanelOpen = new HashMap<>();
    private HashSet<UUID> editing = new HashSet<>();

    public ElevatorHandler() {
        Elevator.panel = Material.valueOf(Elevators.getInstance().getConfig().getString("Elevator.Panel"));
        Elevator.defaultDoor = Material.valueOf(Elevators.getInstance().getConfig().getString("Elevator.Default_Door"));
        Elevator.elevatorRadius = Elevators.getInstance().getConfig().getInt("Elevator.Elevator_Radius");
        Elevator.openDoor = Sound.valueOf(Elevators.getInstance().getConfig().getString("Elevator.Sounds.Door_Open"));
        Elevator.closeDoor = Sound.valueOf(Elevators.getInstance().getConfig().getString("Elevator.Sounds.Door_Close"));
        Elevator.volume = (float) Elevators.getInstance().getConfig().getDouble("Elevators.Sounds.Volume");
        Elevator.pitch = (float) Elevators.getInstance().getConfig().getDouble("Elevators.Sounds.Pitch");
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;

        Player player = event.getPlayer();
        Elevator elevator = Elevator.byBlock(event.getClickedBlock());
        if (elevator == null) return;

        if (player.getGameMode().equals(GameMode.CREATIVE) && player.isSneaking()) {
            edit(player, elevator);
            return;
        }

        open(player, elevator);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (!hasUIOpen(player)) return;

        if (event.getCurrentItem() == null) return;

        Elevator elevator = getElevator(player);

        if (isEditing(player)) {
            if (event.getSlot() != 1)
                event.setCancelled(true);
            if (event.getSlot() == 0) {
                elevator.nextDirection();
                edit(player, elevator);
            }
            return;
        }

        event.setCancelled(true);

        int currentFloor = elevator.getClosestFloor(player.getLocation());
        int selectedFloor = event.getSlot() + 1;

        elevator.move(currentFloor, selectedFloor);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();

        if (isEditing(player)) {
            Elevator elevator = getElevator(player);
            ItemStack door = event.getInventory().getItem(1);
            elevator.setDoor(door.getType(), (byte) door.getDurability());
        }

        elevatorPanelOpen.remove(player.getUniqueId());
        editing.remove(player.getUniqueId());
    }

    public void open(Player player, Elevator elevator) {
        int size = 9;
        while (size < elevator.getTotalFloors()) size += 9;
        if (size > 54) size = 54;
        Inventory inventory = Bukkit.createInventory(null, size, "Elevator");

        for (int floor = 1; floor <= elevator.getTotalFloors(); floor++)
            inventory.addItem(Elevator.getIcon(floor));

        player.openInventory(inventory);
        elevatorPanelOpen.put(player.getUniqueId(), elevator);
    }

    public void edit(Player player, Elevator elevator) {
        Inventory inventory = Bukkit.createInventory(null, 9, "Elevator " + elevator.getX() + ":" + elevator.getZ());

        ItemStack directionIcon = new ItemStack(Material.COMPASS);
        ItemMeta itemMeta = directionIcon.getItemMeta();
        itemMeta.setDisplayName(Elevators.MAIN + "Direction: " + Elevators.SECOND + elevator.getDirection().toString());
        directionIcon.setItemMeta(itemMeta);

        ItemStack doorIcon = new ItemStack(elevator.getDoorMaterial());
        itemMeta = doorIcon.getItemMeta();

        try {
            itemMeta.setDisplayName(Elevators.MAIN + "Door: " + Elevators.SECOND + elevator.getDoorMaterial().toString() + ":" + elevator.getDoorData());
        } catch (Exception e) {
            e.printStackTrace();
        }

        doorIcon.setItemMeta(itemMeta);
        doorIcon.setDurability(elevator.getDoorData());

        inventory.setItem(0, directionIcon);
        inventory.setItem(1, doorIcon);

        player.openInventory(inventory);
        elevatorPanelOpen.put(player.getUniqueId(), elevator);
        editing.add(player.getUniqueId());
    }

    public boolean hasUIOpen(Player player) {
        return getElevator(player) != null;
    }

    public boolean isEditing(Player player) {
        return editing.contains(player.getUniqueId());
    }

    public Elevator getElevator(Player player) {
        return elevatorPanelOpen.getOrDefault(player.getUniqueId(), null);
    }
}