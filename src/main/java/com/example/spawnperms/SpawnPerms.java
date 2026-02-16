package com.example.spawnperms;

import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class SpawnPerms extends JavaPlugin implements Listener, CommandExecutor {

    private int radius;
    private boolean pvpEnabled;
    private String worldName;
    private final Set<UUID> allowed = new HashSet<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadData();
        Bukkit.getPluginManager().registerEvents(this, this);

        getCommand("spawnperms").setExecutor(this);
        getCommand("spawnradius").setExecutor(this);
        getCommand("spawnpvp").setExecutor(this);
    }

    private void loadData() {
        FileConfiguration cfg = getConfig();
        radius = cfg.getInt("radius", 0);
        pvpEnabled = cfg.getBoolean("pvp_enabled", false);
        worldName = cfg.getString("world", "world");

        allowed.clear();
        for (String s : cfg.getStringList("allowed_players")) {
            allowed.add(UUID.fromString(s));
        }
    }

    private void saveAllowed() {
        List<String> list = new ArrayList<>();
        for (UUID id : allowed) list.add(id.toString());
        getConfig().set("allowed_players", list);
        saveConfig();
    }

    private boolean isInsideSpawn(Location loc) {
        if (radius <= 0) return false;
        if (!loc.getWorld().getName().equals(worldName)) return false;

        Location spawn = loc.getWorld().getSpawnLocation();
        return Math.abs(loc.getBlockX() - spawn.getBlockX()) <= radius &&
               Math.abs(loc.getBlockZ() - spawn.getBlockZ()) <= radius;
    }

    private boolean canBuild(Player p) {
        return p.isOp() || allowed.contains(p.getUniqueId());
    }

    /* ---------- BLOCK EVENTS ---------- */

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (isInsideSpawn(e.getBlock().getLocation()) && !canBuild(e.getPlayer())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (isInsideSpawn(e.getBlock().getLocation()) && !canBuild(e.getPlayer())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getClickedBlock() == null) return;

        if (isInsideSpawn(e.getClickedBlock().getLocation()) && !canBuild(e.getPlayer())) {
            e.setCancelled(true);
        }
    }

    /* ---------- PVP ---------- */

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player attacker)) return;
        if (!(e.getEntity() instanceof Player victim)) return;

        if (isInsideSpawn(victim.getLocation()) && !pvpEnabled) {
            e.setCancelled(true);
        }
    }

    /* ---------- COMMANDS ---------- */

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!sender.isOp()) {
            sender.sendMessage(ChatColor.RED + "OP only.");
            return true;
        }

        switch (cmd.getName().toLowerCase()) {

            case "spawnperms":
                if (args.length == 0) {
                    sender.sendMessage("/spawnperms add <player>");
                    sender.sendMessage("/spawnperms remove <player>");
                    sender.sendMessage("/spawnperms list");
                    return true;
                }

                if (args[0].equalsIgnoreCase("list")) {
                    sender.sendMessage(ChatColor.YELLOW + "Allowed players:");
                    for (UUID id : allowed) {
                        OfflinePlayer p = Bukkit.getOfflinePlayer(id);
                        sender.sendMessage("- " + p.getName());
                    }
                    return true;
                }

                if (args.length == 2) {
                    Player target = Bukkit.getPlayer(args[1]);
                    if (target == null) {
                        sender.sendMessage("Player not online.");
                        return true;
                    }

                    if (args[0].equalsIgnoreCase("add")) {
                        allowed.add(target.getUniqueId());
                        saveAllowed();
                        sender.sendMessage("Added " + target.getName());
                    }

                    if (args[0].equalsIgnoreCase("remove")) {
                        allowed.remove(target.getUniqueId());
                        saveAllowed();
                        sender.sendMessage("Removed " + target.getName());
                    }
                }

                return true;

            case "spawnradius":
                if (args.length != 1) {
                    sender.sendMessage("/spawnradius <number>");
                    return true;
                }

                radius = Integer.parseInt(args[0]);
                getConfig().set("radius", radius);
                saveConfig();
                sender.sendMessage("Spawn radius set to " + radius);
                return true;

            case "spawnpvp":
                if (args.length != 1) {
                    sender.sendMessage("/spawnpvp on|off");
                    return true;
                }

                pvpEnabled = args[0].equalsIgnoreCase("on");
                getConfig().set("pvp_enabled", pvpEnabled);
                saveConfig();

                sender.sendMessage("Spawn PVP is now " + (pvpEnabled ? "ON" : "OFF"));
                return true;
        }

        return true;
    }
}