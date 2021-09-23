package com.emmad.mcdiscord;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;

public class MCDiscordCommandExecutor implements CommandExecutor {
    private final MCDiscord plugin;
    private final CoordinateManager coordinateManager;

    public MCDiscordCommandExecutor(MCDiscord plugin, CoordinateManager coordinateManager) {
        this.plugin = plugin;
        this.coordinateManager = coordinateManager;

        this.plugin.getCommand("save-coord").setExecutor(this);
        this.plugin.getCommand("delete-coord").setExecutor(this);
        this.plugin.getCommand("list-coords").setExecutor(this);
        this.plugin.getCommand("get-coord").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("save-coord")) {
            return this.handleSaveCoord(sender, command, label, args);
        }
        if (command.getName().equalsIgnoreCase("delete-coord")) {
            return this.handleDeleteCoord(sender, command, label, args);
        }
        if (command.getName().equalsIgnoreCase("list-coords")) {
            return this.handleListCoords(sender, command, label, args);
        }
        if (command.getName().equalsIgnoreCase("get-coord")) {
            return this.handleGetCoord(sender, command, label, args);
        }
        return false;
    }

    private boolean handleSaveCoord(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Command must be run by a player.");
            return true;
        }
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Must specify a one word name for this coordinate.");
            return false;
        }
        if (args.length > 1 && args.length < 4) {
            sender.sendMessage(ChatColor.RED + "Incorrect number of arguments. Make sure your coordinate name " +
                    "is only one word and that you are specifying either 0 or 3 coordinates (x y z).");
            return false;
        }
        if (args.length > 4) {
            sender.sendMessage(ChatColor.RED + "Incorrect number of arguments. Make sure your coordinate name " +
                    "is only one word and that you are specifying either 0 or 3 coordinates (x y z).");
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
                sender.sendMessage(ChatColor.RED + "Coordinates must be valid decimal numbers.");
                return false;
            }
        }

        try {
            coordinateManager.saveCoordinate(name, player.getName(), location);
            sender.sendMessage(ChatColor.GREEN + "Coordinate saved.");
        } catch (CoordinateManager.DuplicateCoordinateException e) {
            sender.sendMessage(ChatColor.RED + e.getMessage());
        } catch (DiscordBot.RequestFailedException e) {
            sender.sendMessage(ChatColor.DARK_RED + "Failed to save coordinate, see server log.");
            this.plugin.getLogger().info(e.getMessage());
        }

        return true;
    }

    private boolean handleDeleteCoord(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Command must be run by a player.");
            return true;
        }
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Must specify a coordinate name.");
            return false;
        }
        if (args.length > 1) {
            sender.sendMessage(ChatColor.RED + "Too many arguments.");
            return false;
        }

        String name = args[0];
        try {
            coordinateManager.deleteCoordinate(name, sender.getName());
            sender.sendMessage(ChatColor.GREEN + "Coordinate deleted.");
        } catch (CoordinateManager.CoordinateDoesNotExistException
                | CoordinateManager.PlayerLacksPermissionException e) {
            sender.sendMessage(ChatColor.RED + e.getMessage());
        } catch (DiscordBot.RequestFailedException e) {
            sender.sendMessage(ChatColor.DARK_RED + "Failed to delete coordinate, see server log.");
            this.plugin.getLogger().info(e.getMessage());
        }
        return true;
    }

    private boolean handleListCoords(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0) {
            sender.sendMessage(ChatColor.RED + "Too many arguments.");
            return false;
        }

        Collection<CoordinateManager.Coordinate> coordinates = coordinateManager.getCoordinates();
        if (coordinates.isEmpty()) {
            sender.sendMessage("There are no saved coordinates. You can save a coordinate with "
                    + ChatColor.AQUA + "/save-cord" + ChatColor.RESET + ".");
        } else {
            for (CoordinateManager.Coordinate coord : coordinates) {
                sender.sendMessage(coord.toMinecraftString());
            }
        }

        return true;
    }

    private boolean handleGetCoord(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Must specify a coordinate name.");
            return false;
        }
        if (args.length > 1) {
            sender.sendMessage(ChatColor.RED + "Too many arguments.");
            return false;
        }

        String name = args[0];
        try {
            CoordinateManager.Coordinate coord = coordinateManager.getCoordinate(name);
            sender.sendMessage(coord.toMinecraftString());
        } catch (CoordinateManager.CoordinateDoesNotExistException e) {
            sender.sendMessage(ChatColor.RED + e.getMessage());
        }

        return true;
    }
}
