package me.therealdan.elevators.mechanics;

import me.therealdan.elevators.Elevators;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.*;

public class Elevator {

    private static File file;
    private static FileConfiguration data;
    private static String path = "elevators.yml";

    private static HashSet<Elevator> elevators = new HashSet<>();

    public static Material panel, defaultDoor;
    public static Sound openDoor, closeDoor;
    public static float volume, pitch;
    public static int elevatorRadius;
    private static ItemStack icon;
    private static String displayName;

    private UUID world;
    private Direction direction;
    private int x;
    private int z;
    private HashSet<Integer> y = new HashSet<>();

    private HashMap<Block, Material> previousMaterial = new HashMap<>();
    private HashMap<Block, Byte> previousData = new HashMap<>();
    private Material door;
    private Byte doorData;

    private Elevator(Location location, Direction direction) {
        this.world = location.getWorld().getUID();
        this.direction = direction;

        this.x = location.getBlockX();
        this.z = location.getBlockZ();
        add(location);

        this.door = defaultDoor;
        this.doorData = 0;

        elevators.add(this);
    }

    private Elevator(String id) {
        this.world = UUID.fromString(getData().getString("Elevators." + id + ".World"));
        this.direction = Direction.valueOf(getData().getString("Elevators." + id + ".Direction"));

        this.x = getData().getInt("Elevators." + id + ".X");
        this.z = getData().getInt("Elevators." + id + ".Z");

        this.door = Material.valueOf(getData().getString("Elevators." + id + ".Door"));
        this.doorData = (byte) getData().getInt("Elevators." + id + ".DoorData");

        elevators.add(this);
    }

    private void save() {
        if (inUse())
            for (int floor = 1; floor <= getTotalFloors(); floor++)
                open(floor);

        String id = UUID.randomUUID().toString();
        getData().set("Elevators." + id + ".World", world.toString());
        getData().set("Elevators." + id + ".Direction", direction.toString());
        getData().set("Elevators." + id + ".X", x);
        getData().set("Elevators." + id + ".Z", z);

        getData().set("Elevators." + id + ".Door", door.toString());
        getData().set("Elevators." + id + ".DoorData", doorData);
    }

    public void move(int floorFrom, int floorTo) {
        if (floorFrom == floorTo) return;
        int totalFloors = getTotalFloors();
        if (floorTo > totalFloors) return;

        for (int floor = 1; floor <= totalFloors; floor++)
            close(floor);

        double distance = getPanel(floorFrom).distance(getPanel(floorTo));
        boolean up = floorFrom < floorTo;

        Location center = getCenter(floorFrom);
        for (Entity entity : center.getWorld().getNearbyEntities(center, elevatorRadius, elevatorRadius, elevatorRadius))
            entity.teleport(entity.getLocation().add(0, (up ? distance : -distance), 0));

        long ticksTillDoorsOpen = 10 + (long) distance;
        Bukkit.getScheduler().runTaskLater(Elevators.getInstance(), new Runnable() {
            @Override
            public void run() {
                for (int floor = 1; floor <= getTotalFloors(); floor++)
                    open(floor);
            }
        }, ticksTillDoorsOpen);
    }

    private void open(int floor) {
        if (!inUse()) return;

        Location center = getCenter(floor);
        if (volume > 0.0) center.getWorld().playSound(center, openDoor, volume, pitch);

        for (Block block : getElevatorDoors(center)) {
            if (previousMaterial.containsKey(block)) block.setType(previousMaterial.get(block));
            if (previousData.containsKey(block)) block.setData(previousData.get(block));
        }

    }

    private void close(int floor) {
        if (inUse()) return;

        Location center = getCenter(floor);
        if (volume > 0.0) center.getWorld().playSound(center, closeDoor, volume, pitch);

        for (Block block : getElevatorDoors(center)) {
            previousMaterial.put(block, block.getType());
            previousData.put(block, block.getData());

            if (block.getType().equals(Material.AIR)) {
                block.setType(getDoorMaterial());
                block.setData(getDoorData());
            }
        }
    }

    public void scan() {
        for (int y = 0; y < getWorld().getHighestBlockYAt(getX(), getZ()); y++) {
            Location location = new Location(getWorld(), getX(), y, getZ());
            if (location.getBlock().getType().equals(Elevator.panel)) add(location);
        }
    }

    public void add(Location location) {
        if (!location.getWorld().getUID().equals(getWorld().getUID())) return;

        if (location.getBlockX() != getX()) return;
        if (location.getBlockZ() != getZ()) return;

        y.add(location.getBlockY());
    }

    public void setDoor(Material material, byte durability) {
        this.door = material;
        this.doorData = durability;
    }

    public void nextDirection() {
        switch (getDirection()) {
            case NORTH:
                this.direction = Direction.EAST;
                return;
            case EAST:
                this.direction = Direction.SOUTH;
                return;
            case SOUTH:
                this.direction = Direction.WEST;
                return;
            case WEST:
                this.direction = Direction.NORTH;
                return;
        }
    }

    public boolean inUse() {
        return previousMaterial.size() > 0;
    }

    public int getTotalFloors() {
        scan();
        return getY().size();
    }

    public int getClosestFloor(Location origin) {
        int currentFloor = 1;
        int closestFloor = 1;

        double closestDistance = Integer.MAX_VALUE;
        for (int y = 0; y < getWorld().getHighestBlockYAt(getX(), getZ()); y++) {
            Location location = new Location(getWorld(), getX(), y, getZ());
            if (location.getBlock().getType().equals(Elevator.panel)) {
                add(location);
                double distance = origin.distance(location);
                if (distance < closestDistance) {
                    closestFloor = currentFloor;
                    closestDistance = distance;
                }
                currentFloor++;
            }
        }

        return closestFloor;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public List<Integer> getY() {
        return new ArrayList<>(y);
    }

    public Location getPanel(int floor) {
        int currentFloor = 1;
        for (int y = 0; y < getWorld().getHighestBlockYAt(getX(), getZ()); y++) {
            Location location = new Location(getWorld(), getX(), y, getZ());
            if (location.getBlock().getType().equals(Elevator.panel)) {
                add(location);
                if (currentFloor == floor)
                    return location;
                currentFloor++;
            }
        }
        return null;
    }

    public Location getCenter(int floor) {
        Location panel = getPanel(floor);
        switch (getDirection()) {
            case NORTH:
                return panel.clone().add(0, 0, -2);
            case SOUTH:
                return panel.clone().add(0, 0, 2);
            case EAST:
                return panel.clone().add(2, 0, 0);
            case WEST:
                return panel.clone().add(-2, 0, 0);
        }
        return null;
    }

    public World getWorld() {
        return Bukkit.getWorld(world);
    }

    public Direction getDirection() {
        return direction;
    }

    public Material getDoorMaterial() {
        if (door == null) door = defaultDoor;
        return door;
    }

    public Byte getDoorData() {
        return doorData;
    }

    public static List<Block> getElevatorDoors(Location center) {
        List<Block> blocks = new ArrayList<>();
        for (int radius : new int[]{elevatorRadius, -elevatorRadius}) {
            for (int y = -(elevatorRadius - 1); y <= (elevatorRadius - 1); y++) {
                for (int xz = -(elevatorRadius - 1); xz <= (elevatorRadius - 1); xz++) {
                    blocks.add(center.clone().add(radius, y, xz).getBlock());
                    blocks.add(center.clone().add(xz, y, radius).getBlock());
                }
            }
        }
        return blocks;
    }

    public static void load() {
        if (getData().contains("Elevators"))
            for (String id : getData().getConfigurationSection("Elevators").getKeys(false))
                new Elevator(id);
    }

    public static void unload() {
        getData().set("Elevators", null);

        for (Elevator elevator : values())
            elevator.save();

        saveData();
    }

    public static ItemStack getIcon(int floor) {
        if (icon == null) {
            icon = new ItemStack(Material.valueOf(Elevators.getInstance().getConfig().getString("Elevator.Icon.Material")));
            icon.setDurability((short) Elevators.getInstance().getConfig().getInt("Elevator.Icon.Durability"));
            displayName = ChatColor.translateAlternateColorCodes('&', Elevators.getInstance().getConfig().getString("Elevator.Icon.Name"));
        }
        ItemMeta itemMeta = icon.getItemMeta();
        itemMeta.setDisplayName(displayName.replace("%floor%", Integer.toString(floor)));
        icon.setItemMeta(itemMeta);
        return icon;
    }

    public static Elevator scan(Location location) {
        if (!location.getBlock().getType().equals(Elevator.panel)) return null;

        Elevator elevator = new Elevator(location, Direction.NORTH);
        for (int y = 0; y < location.getWorld().getHighestBlockYAt(location); y++) {
            Block block = location.getWorld().getBlockAt(elevator.getX(), y, elevator.getZ());
            if (block.getType().equals(Elevator.panel)) {
                elevator.add(block.getLocation());
            }
        }
        return null;
    }

    public static Elevator byBlock(Block block) {
        if (block == null) return null;
        return byLocation(block.getLocation());
    }

    public static Elevator byLocation(Location location) {
        if (!location.getBlock().getType().equals(Elevator.panel)) return null;

        for (Elevator elevator : values())
            if (elevator.getWorld().getUID().equals(location.getWorld().getUID()))
                if (elevator.getX() == location.getBlockX() && elevator.getZ() == location.getBlockZ())
                    return elevator;

        return scan(location);
    }

    public static List<Elevator> values() {
        return new ArrayList<>(elevators);
    }

    private static void saveData() {
        try {
            getData().save(getFile());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static FileConfiguration getData() {
        if (data == null) data = YamlConfiguration.loadConfiguration(getFile());
        return data;
    }

    private static File getFile() {
        if (file == null) file = new File(Elevators.getInstance().getDataFolder(), path);
        return file;
    }

    public enum Direction {
        NORTH, EAST, SOUTH, WEST
    }
}