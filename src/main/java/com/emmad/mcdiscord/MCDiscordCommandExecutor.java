package com.emmad.mcdiscord;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MCDiscordCommandExecutor implements CommandExecutor {
    private final MCDiscord plugin;

    public MCDiscordCommandExecutor(MCDiscord plugin) {
        this.plugin = plugin;
        this.plugin.getCommand("save-coord").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("save-coord")) {
            return this.handleSaveCoord(sender, command, label, args);
        }
        return false;
    }

    private boolean handleSaveCoord(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Command must be run by a player.");
            return false;
        }
        if (args.length < 1) {
            sender.sendMessage("Must specify a name for this coordinate.");
            return false;
        }
        if (args.length > 1 && args.length < 4) {
            sender.sendMessage("Must specify all three coordinates (x, y, z).");
            return false;
        }
        if (args.length > 4) {
            sender.sendMessage("Too many arguments.");
            return false;
        }

        Location location;
        String name = args[0];
        Player player = (Player) sender;

        if (args.length == 1) {
            location = player.getLocation();
        } else {
            try {
                double x = Double.parseDouble(args[1]);
                double y = Double.parseDouble(args[2]);
                double z = Double.parseDouble(args[3]);
                location = new Location(player.getWorld(), x, y, z);
            } catch (NumberFormatException e) {
                sender.sendMessage("Coordinates must be valid decimal numbers.");
                return false;
            }
        }

        CoordManager.saveCoord(name, location);
        this.plugin.getLogger().info(name + " " + location.getBlockX() + " " + location.getBlockY() + " " + location.getBlockZ());

        sender.sendMessage("Coordinate saved.");
        return true;
    }
}
