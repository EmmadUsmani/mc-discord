package com.emmad.mcdiscord.coordinate;

import com.emmad.mcdiscord.DiscordBot;
import com.emmad.mcdiscord.Main;
import com.emmad.mcdiscord.util.CommandResponse;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;

public class CoordinateCommandExecutor implements CommandExecutor {
    private final JavaPlugin plugin;
    private final CoordinateManager coordinateManager;

    public CoordinateCommandExecutor(Main plugin, CoordinateManager coordinateManager) throws InitializationFailedException {
        this.plugin = plugin;
        this.coordinateManager = coordinateManager;

        try {
            this.plugin.getCommand("save-coord").setExecutor(this);
            this.plugin.getCommand("delete-coord").setExecutor(this);
            this.plugin.getCommand("list-coords").setExecutor(this);
            this.plugin.getCommand("get-coord").setExecutor(this);
        } catch (NullPointerException e) {
            throw new InitializationFailedException("NullPointerException while calling setExecutor." +
                    " Developer, make sure commands are defined in plugin.yml.");
        }

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("save-coord")) {
            return handleSaveCoord(sender, command, label, args);
        }
        if (command.getName().equalsIgnoreCase("delete-coord")) {
            return handleDeleteCoord(sender, command, label, args);
        }
        if (command.getName().equalsIgnoreCase("list-coords")) {
            return handleListCoords(sender, command, label, args);
        }
        if (command.getName().equalsIgnoreCase("get-coord")) {
            return handleGetCoord(sender, command, label, args);
        }
        return false;
    }

    private boolean handleSaveCoord(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            CommandResponse.userError(sender, "Command must be run by a player.");
            return true;
        }
        if (args.length < 1) {
            CommandResponse.userError(sender, "Must specify a one word name for this coordinate.");
            return false;
        }
        if (args.length > 1 && args.length < 4) {
            CommandResponse.userError(sender, "Incorrect number of arguments. Make sure your coordinate name " +
                    "is only one word and that you are specifying either 0 or 3 coordinates (x y z).");
            return false;
        }
        if (args.length > 4) {
            CommandResponse.userError(sender, "Incorrect number of arguments. Make sure your coordinate name " +
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
                CommandResponse.userError(sender, "Coordinates must be valid decimal numbers.");
                return false;
            }
        }

        try {
            coordinateManager.saveCoordinate(name, player.getName(), location);
            CommandResponse.confirmation(sender, "Coordinate saved.");
        } catch (CoordinateManager.DuplicateCoordinateException e) {
            CommandResponse.userError(sender, e.getMessage());
        } catch (DiscordBot.RequestFailedException e) {
            CommandResponse.serverError(sender, "Failed to save coordinate, see server log.");
            plugin.getLogger().info(e.getMessage());
        }

        return true;
    }

    private boolean handleDeleteCoord(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            CommandResponse.userError(sender, "Command must be run by a player.");
            return true;
        }
        if (args.length < 1) {
            CommandResponse.userError(sender, "Must specify a coordinate name.");
            return false;
        }
        if (args.length > 1) {
            CommandResponse.userError(sender, "Too many arguments.");
            return false;
        }

        String name = args[0];
        try {
            coordinateManager.deleteCoordinate(name, sender.getName());
            CommandResponse.confirmation(sender, "Coordinate deleted.");
        } catch (CoordinateManager.CoordinateDoesNotExistException
                | CoordinateManager.PlayerLacksPermissionException e) {
            CommandResponse.userError(sender, e.getMessage());
        } catch (DiscordBot.RequestFailedException e) {
            CommandResponse.serverError(sender, "Failed to delete coordinate, see server log.");
            plugin.getLogger().info(e.getMessage());
        }
        return true;
    }

    private boolean handleListCoords(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0) {
            CommandResponse.userError(sender, "Too many arguments.");
            return false;
        }

        Collection<CoordinateManager.Coordinate> coordinates = coordinateManager.getCoordinates();
        if (coordinates.isEmpty()) {
            CommandResponse.info(sender, "There are no saved coordinates. You can save a coordinate with "
                    + ChatColor.AQUA + "/save-cord" + ChatColor.RESET + ".");
        } else {
            for (CoordinateManager.Coordinate coordinate : coordinates) {
                CommandResponse.info(sender, coordinate.toMinecraftString());
            }
        }

        return true;
    }

    private boolean handleGetCoord(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            CommandResponse.userError(sender, "Must specify a coordinate name.");
            return false;
        }
        if (args.length > 1) {
            CommandResponse.userError(sender, "Too many arguments.");
            return false;
        }

        String name = args[0];
        try {
            CoordinateManager.Coordinate coordinate = coordinateManager.getCoordinate(name);
            CommandResponse.info(sender, coordinate.toMinecraftString());
        } catch (CoordinateManager.CoordinateDoesNotExistException e) {
            CommandResponse.userError(sender, e.getMessage());
        }

        return true;
    }

    public static class InitializationFailedException extends Exception {
        public InitializationFailedException(String message) {
            super(message);
        }
    }
}
