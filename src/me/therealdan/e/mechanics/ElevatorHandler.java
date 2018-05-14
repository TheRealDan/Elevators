package me.therealdan.e.mechanics;

import me.therealdan.e.Elevators;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;

/**
 * Created by Daniel on 13/01/2017.
 */
public class ElevatorHandler {

    private HashMap<Player, String> uiOpen = new HashMap<>();
    private HashMap<Player, String> elevator = new HashMap<>();

    public long doorA = 20, rate = 40, multiplier = 5;

    public void onInteract(Elevators m, PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock().getType() != Material.JUKEBOX) return;

        Player player = event.getPlayer();
        Location location = event.getClickedBlock().getLocation();
        event.setCancelled(true);

        if (isElevator(m, location, false)) {
            if (player.getGameMode() == GameMode.CREATIVE && player.getEquipment().getItemInMainHand().getType() == Material.JUKEBOX) {
                m.editElevator.open(m, player, location);
            } else {
                open(m, player, location);
            }
        }
    }

    public void onPlace(Elevators m, BlockPlaceEvent event) {
        if (event.getBlockPlaced().getType() != Material.JUKEBOX) return;
        if (event.isCancelled()) return;

        if (isElevator(m, event.getBlockPlaced().getLocation(), false)) return;

        save(m, event.getBlockPlaced().getLocation(), Direction.NORTH, false);
        save(m, event.getBlockPlaced().getLocation(), Material.GLASS, true);
    }

    public void onClick(Elevators m, InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (!uiOpen.containsKey(player)) return;
        event.setCancelled(true);
        if (event.getCurrentItem().getType() != Material.STAINED_GLASS_PANE) return;

        int target = event.getSlot() + 1;
        player.closeInventory();

        int floor = 0;
        Location location = player.getLocation();
        location.setX(Integer.parseInt(elevator.get(player).split(";")[0]));
        location.setZ(Integer.parseInt(elevator.get(player).split(";")[1]));
        for (int y = 0; y < 255; y++) {
            location.setY(y);
            if (isElevator(m, location, true)) {
                floor++;
                if (floor == target) {
                    location.setX(player.getLocation().getX());
                    location.setZ(player.getLocation().getZ());
                    location.add(0, -1, 0);
                    travel(m, player, location);
                }
            }
        }
    }

    public void onClose(Elevators m, InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        uiOpen.remove(player);
    }

    private void open(Elevators m, Player player, Location location) {
        elevator.put(player, location.getBlockX() + ";" + location.getBlockZ());

        Inventory inventory = Bukkit.createInventory(null, 18, "Elevator");
        player.openInventory(inventory);
        uiOpen.put(player, "");

        int floor = 0;
        for (int y = 0; y < 255; y++) {
            location.setY(y);
            if (isElevator(m, location, true)) {
                floor++;
                inventory.addItem(floor(m, player.getLocation().getBlockY() - y <= 2 && player.getLocation().getBlockY() - y >= -2, floor));
            }
        }
    }

    private void travel(Elevators m, Player player, Location location) {
        boolean first = true;

        // Grab Nearby Entities
        HashMap<Entity, Player> entities = new HashMap<>();
        for (Entity entity : player.getNearbyEntities(2, 3, 2))
            if (entity != player) entities.put(entity, player);

        // Get distance, Transport player
        int distance = (int) player.getLocation().distance(location);
        player.teleport(location);

        // Transport mechanics contents
        Location loc;
        for (Entity entity : entities.keySet()) {
            loc = entity.getLocation().clone();
            loc.setY(player.getLocation().getY());
            entity.teleport(loc);
        }

        // Find Door A
        final HashMap<Block, String> blockListA = new HashMap<>();
        // TODO

        // Find Door B
        final HashMap<Block, String> blockListB = new HashMap<>();
        // TODO

        // Close Door A
        final HashMap<Block, Material> materialsA = new HashMap<>();
        final HashMap<Block, Byte> blockDataA = new HashMap<>();
        for (final Block block : blockListA.keySet()) {
            if (first) {
                first = false;
                block.getWorld().playSound(block.getLocation(), Sound.BLOCK_WOODEN_DOOR_CLOSE, 1, 1);
            }
            materialsA.put(block, block.getType());
            blockDataA.put(block, block.getData());
            block.setType(Material.AIR);
        }
        first = true;

        // Close Door B
        final HashMap<Block, Material> materialsB = new HashMap<>();
        final HashMap<Block, Byte> blockDataB = new HashMap<>();
        for (final Block block : blockListA.keySet()) {
            if (first) {
                first = false;
                block.getWorld().playSound(block.getLocation(), Sound.BLOCK_WOODEN_DOOR_CLOSE, 1, 1);
            }
            materialsB.put(block, block.getType());
            blockDataB.put(block, block.getData());
            block.setType(Material.AIR);
        }
        first = true;

        // Open Door A
        Bukkit.getScheduler().scheduleSyncDelayedTask(m, new Runnable() {
            public void run() {
                boolean first = true;
                for (final Block block : blockListA.keySet()) {
                    if (first) {
                        first = false;
                        block.getWorld().playSound(block.getLocation(), Sound.BLOCK_WOODEN_DOOR_CLOSE, 1, 1);
                    }
                    block.setType(materialsA.get(block));
                    block.setData(blockDataA.get(block));
                }
            }
        }, doorA);

        // Open Door B
        Bukkit.getScheduler().scheduleSyncDelayedTask(m, new Runnable() {
            public void run() {
                boolean first = true;
                for (final Block block : blockListA.keySet()) {
                    if (first) {
                        first = false;
                        block.getWorld().playSound(block.getLocation(), Sound.BLOCK_WOODEN_DOOR_CLOSE, 1, 1);
                    }
                    block.setType(materialsB.get(block));
                    block.setData(blockDataB.get(block));
                }
            }
        }, (distance * multiplier) + rate);
    }

    private void save(Elevators m, Location location, Direction direction, boolean save) {
        m.ElevatorData.set("Elevators." + location.getBlockX() + ";" + location.getBlockZ() + ".Direction", direction.toString());
        if (save) m.saveElevatorData();
    }

    private void save(Elevators m, Location location, Material material, boolean save) {
        m.ElevatorData.set("Elevators." + location.getBlockX() + ";" + location.getBlockZ() + ".Material", material.toString());
        if (save) m.saveElevatorData();
    }

    private boolean isElevator(Elevators m, Location location, boolean exact) {
        if (exact && location.getBlock().getType() != Material.JUKEBOX) return false;
        return m.ElevatorData.contains("Elevators." + location.getBlockX() + ";" + location.getBlockZ());
    }

    private ItemStack floor(Elevators m, boolean current, int floor) {
        ItemStack itemStack = new ItemStack(Material.STAINED_GLASS_PANE);
        if (current) itemStack.setDurability((short) 15);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "Floor " + floor));
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public enum Direction {
        NORTH, EAST, SOUTH, WEST
    }
}