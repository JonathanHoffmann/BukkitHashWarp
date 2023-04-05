package me.Jonnyfant.BukkitHashWarp;

//https://bukkit.org/threads/hash-warp.500574/

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Random;

public class BukkitHashWarp extends JavaPlugin {
    private final String CFG_KEY_CHECK_AIR = "OnlyTeleportToAirBlocks";
    private final boolean CFG_DEFAULT_CHECK_AIR = true;
    private final String CFG_KEY_RADIUS = "_WarpRadius";
    private final int CFG_DEFAULT_RADIUS = 29999980;
    private final int MIN_WORLD_HEIGHT = -64;
    private final String PERM_USE = "hashwarp.use";
    private final String PERM_ADMIN = "hashwarp.admin";
    private final String PERM_REVERSE = "hashwarp.reverse";
    private final String PERM_LIST = "hashwarp.list";

    public void onEnable() {
        loadConfig();
    }

    public void loadConfig() {
        getConfig().addDefault(CFG_KEY_CHECK_AIR, CFG_DEFAULT_CHECK_AIR);
        for (World w : Bukkit.getWorlds()) {
            getConfig().addDefault(w.getName() + CFG_KEY_RADIUS, CFG_DEFAULT_RADIUS);
        }
        getConfig().options().copyDefaults(true);
        saveConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String commandName = command.getName();
        switch (commandName.toLowerCase()) {
            case "warp":
                try {
                    return warp(sender, args);
                } catch (IOException e) {
                    return false;
                }
            case "warpreverse":
                return reverseWarp(sender, args);
            case "warpchangeradius":
                return changeWarpRadius(sender, args);
            case "warplist":
                return listWarps(sender, args);
            default:
                return false;
        }
    }

    private boolean listWarps(CommandSender sender, String[] args) {
        if (!sender.hasPermission(PERM_LIST)) {
            sender.sendMessage("You don't have the permission " + PERM_LIST);
            return false;
        }
        String world = "";
        if (args.length > 0) {
            world = args[0];
        } else if (sender instanceof Player) {
            world = ((Player) sender).getWorld().getName();
        } else {
            sender.sendMessage("Non Players have to define a world.");
            return false;
        }
        reloadConfig();
        File baseFolder = this.getDataFolder();
        File worldFolder = new File(baseFolder, world);
        if (!worldFolder.exists()) {
            sender.sendMessage("This world does not exist, or there are no used warps in this world.");
            return false;
        }
        File[] files = worldFolder.listFiles();
        String m = "";
        for (File f : files) {
            YamlConfiguration warp = YamlConfiguration.loadConfiguration(f);
            m += warp.getString("name") + ": X=" + warp.getInt("warpX") + " Z=" + warp.getInt("warpZ") + "\n";
        }
        if (m.equals(""))
            m = "There are no warps for " + world;
        sender.sendMessage(m);
        return true;
    }

    private boolean changeWarpRadius(CommandSender sender, String[] args) {
        if (!sender.hasPermission(PERM_ADMIN)) {
            sender.sendMessage("You don't have the permission " + PERM_ADMIN);
            return false;
        }
        if (args.length < 2) {
            sender.sendMessage("Too few arguments.");
            return false;
        }
        ArrayList<String> worldNames = new ArrayList<>();
        for (World w : Bukkit.getWorlds()) {
            worldNames.add(w.getName());
        }
        if (!worldNames.contains(args[0])) {
            sender.sendMessage("Cannot find world: " + args[0]);
            return false;
        }
        try {
            getConfig().set(args[0] + CFG_KEY_RADIUS, Integer.parseInt(args[1]));
            saveConfig();
        } catch (NumberFormatException e) {
            sender.sendMessage(args[1] + " is not a valid number.");
        }
        return false;
    }

    private boolean reverseWarp(CommandSender sender, String[] args) {
        if (!sender.hasPermission(PERM_REVERSE)) {
            sender.sendMessage("You don't have the permission " + PERM_REVERSE);
            return false;
        }
        ArrayList<String> worldNames = new ArrayList<>();
        String searchworld;
        // args should be world x z
        if (args.length == 2) {
            if (sender instanceof Player) {
                searchworld = ((Player) sender).getWorld().getName();
            } else {
                sender.sendMessage("You need to provide a world");
                return false;
            }
        } else if (args.length >= 3) {
            searchworld = args[2];
        } else {
            sender.sendMessage("Too few arguments.");
            return false;
        }


        for (World w : Bukkit.getWorlds()) {
            worldNames.add(w.getName());
        }
        if (!worldNames.contains(searchworld)) {
            sender.sendMessage("The world " + searchworld + " doesn't exist or has no warps");
            return false;
        }
        int searchX;
        int searchZ;
        try {
            searchX = Integer.parseInt(args[0]);
            searchZ = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage("X and Z have to be whole numbers.");
            return false;
        }
        String ret = searchFiles(searchworld, searchX, searchZ);
        if (ret != null) {
            sender.sendMessage("The warp for X:" + searchX + " Z:" + searchZ + " is " + ret);
            return true;
        } else {
            sender.sendMessage("There is no warp for X:" + searchX + " Z:" + searchZ);
            return false;
        }

    }

    public String searchFiles(String world, int x, int z) {
        reloadConfig();
        File baseFolder = this.getDataFolder();
        File worldFolder = new File(baseFolder, world);
        if (!worldFolder.exists()) //shouldn't ever be true
            return null;
        File[] files = worldFolder.listFiles();
        for (File f : files) {
            YamlConfiguration warp = YamlConfiguration.loadConfiguration(f);
            int xf = warp.getInt("warpX");
            int zf = warp.getInt("warpZ");
            if (xf == x && zf == z) {
                return warp.getString("name");
            }
        }
        return null;
    }

    public boolean warp(CommandSender sender, String[] args) throws IOException {
        if (sender instanceof Player == false) {
            sender.sendMessage("This command is for Players only, dummy.");
            return false;
        }
        if (!sender.hasPermission(PERM_USE)) {
            sender.sendMessage("You don't have the permission " + PERM_USE);
            return false;
        }
        if (args.length < 1) {
            sender.sendMessage("You need to add a name for your warp.");
            return false;
        }
        if (args[0] == null) {
            sender.sendMessage("How do you even send empty arguments?!");
            return false;
        }
        Player p = (Player) sender;
        reloadConfig();
        File warpWorldFolder = new File(this.getDataFolder(), p.getWorld().getName());
        if (!warpWorldFolder.exists())
            warpWorldFolder.mkdir();
        File warpfile = new File(warpWorldFolder, args[0] + ".yml");
        //create a new warp
        if (!warpfile.exists()) {
            Random r = new Random();
            int bounds = getConfig().getInt(p.getWorld().getName() + "WarpRadius");
            int rx = r.nextInt(bounds);
            int rz = r.nextInt(bounds);
            //randomly make x and z negative
            if (r.nextDouble() > 0.5)
                rx = rx * -1;
            if (r.nextDouble() > 0.5)
                rz = rz * -1;
            YamlConfiguration warp = YamlConfiguration.loadConfiguration(warpfile);
            warp.set("name", args[0]);
            warp.set("warpX", rx);
            warp.set("warpZ", rz);
            warp.set("warpWorld", p.getLocation().getWorld().getName());
            warp.save(warpfile);
        }
        YamlConfiguration warp = YamlConfiguration.loadConfiguration(warpfile);
        return teleportToWarp(warp, p);
    }

    public boolean teleportToWarp(YamlConfiguration warp, Player p) {
        Location location = p.getLocation();
        location.setX(warp.getDouble("warpX") + 0.5);
        location.setZ(warp.getDouble("warpZ") + 0.5);
        location.setWorld(Bukkit.getWorld(warp.getString("warpWorld")));

        for (int i = location.getWorld().getMaxHeight(); i > MIN_WORLD_HEIGHT; i--) {
            location.setY(i - 1);
            if (!(location.getBlock().isPassable()) && !(location.getBlock().getType().equals(Material.BEDROCK))) { //could add checks for not spawning in lava or on nether roof, bedrock
                location.setY(i);
                if (getConfig().getBoolean(CFG_KEY_CHECK_AIR)) {
                    Location l1 = location.clone();
                    Location l2 = location.clone();
                    l1.setY(l1.getY() + 0.5);
                    l2.setY(l2.getY() + 1.5);
                    if ((l1.getBlock().getType().equals(Material.AIR) || l1.getBlock().getType().equals(Material.LEGACY_AIR))
                            && (l2.getBlock().getType().equals(Material.AIR) || l2.getBlock().getType().equals(Material.LEGACY_AIR))) { //teleport
                    } else {
                        continue;
                    }
                }

                p.teleport(location);
                p.sendMessage("Teleporting you to " + warp.getString("name") + " (X: " + new BigDecimal(location.getX() - 0.5).toPlainString() + " Y: "
                        + location.getY() + " Z: " + new BigDecimal(location.getZ() - 0.5).toPlainString() + ")");
                return true;
            }
        }
        p.sendMessage("Couldn't find a block to spawn you on: " + warp.getString("name") + " (X: " + location.getX()
                + " Y: " + (int) location.getY() + " Z: " + location.getZ() + ")");
        return false;
    }
}
