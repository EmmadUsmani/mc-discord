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
        this.plugin.getCommand("savecoord").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("savecoord")) {
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
        if (args.length > 1) {
            sender.sendMessage("Too many arguments.");
            return false;
        }

        String name = args[0];
        Player player = (Player) sender;
        Location location = player.getLocation();
        this.plugin.getLogger().info(name + " " + location.getX() + " " + location.getY() + " " + location.getZ());

        sender.sendMessage("Coordinate saved.");
        return true;
    }
}
