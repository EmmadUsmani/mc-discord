package com.emmad.mcdiscord.coordinate;

import com.emmad.mcdiscord.DiscordBot;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.javacord.api.entity.message.Message;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class CoordinateManager {
    private final JavaPlugin plugin;
    private final DiscordBot discordBot;
    private final Map<String, Coordinate> coordinateMap;

    public CoordinateManager(JavaPlugin plugin, DiscordBot discordBot) throws InitializationFailedException {
        this.plugin = plugin;
        this.discordBot = discordBot;
        this.coordinateMap = new HashMap<>();

        try {
            loadCoordinates();
        } catch (DiscordBot.RequestFailedException e){
            throw new InitializationFailedException("Failed to load coordinates from Discord.");
        }
    }

    public void loadCoordinates() throws DiscordBot.RequestFailedException {
        Message[] messages = discordBot.getCoordinateMessages();

        for (Message message : messages) {
            try {
                Coordinate coordinate = deserializeCoordinate(message);
                if (coordinateMap.containsKey(coordinate.name)) {
                    throw new DuplicateCoordinateException(coordinate.name);
                } else {
                    coordinate.setMessageId(message.getIdAsString());
                    coordinateMap.put(coordinate.name, coordinate);
                }
            } catch (Exception e) {
                plugin.getLogger().warning(e.getMessage());
            }
        }
    }

    public void saveCoordinate(String name, String addedBy, Location location)
            throws DuplicateCoordinateException, DiscordBot.RequestFailedException {
        name = normalizeName(name);
        if (coordinateMap.containsKey(name)) {
            throw new DuplicateCoordinateException(name);
        }

        Coordinate coordinate = new Coordinate(name, addedBy, location);
        discordBot.postCoordinateMessage(coordinate);
        coordinateMap.put(name, coordinate);
    }

    public void deleteCoordinate(String name, String playerName)
            throws CoordinateDoesNotExistException, PlayerLacksPermissionException, DiscordBot.RequestFailedException {
        name = normalizeName(name);
        if (!coordinateMap.containsKey(name)) {
            throw new CoordinateDoesNotExistException(name);
        }

        Coordinate coordinate = coordinateMap.get(name);
        if (!playerName.equals(coordinate.addedBy)) {
            throw new PlayerLacksPermissionException(coordinate.addedBy);
        }

        discordBot.deleteCoordinateMessage(coordinate);
        coordinateMap.remove(name);
    }

    public Collection<Coordinate> getCoordinates() {
        return coordinateMap.values();
    }

    public Coordinate getCoordinate(String name) throws CoordinateDoesNotExistException {
        name = normalizeName(name);
        if (!coordinateMap.containsKey(name)) {
            throw new CoordinateDoesNotExistException(name);
        }
        return coordinateMap.get(name);
    }

    private static Coordinate deserializeCoordinate(Message message) throws InvalidMessageException {
        String messageContent = message.getContent();
        String[] lines = messageContent.split("\n");
        if (lines.length != 3) {
            throw new InvalidMessageException(message);
        }

        String name = lines[0];
        String addedBy = lines[1];

        String locationStr = lines[2];
        String[] values = locationStr.split(" ");
        if (values.length != 3) {
            throw new InvalidMessageException(message);
        }
        double[] doubleValues;
        try {
            doubleValues = Arrays.stream(values).mapToDouble(Double::parseDouble).toArray();
        } catch (NumberFormatException e) {
            throw new InvalidMessageException(message);
        }
        Location location = new Location(Bukkit.getWorld("world"), doubleValues[0],
                doubleValues[1], doubleValues[2]);

        return new Coordinate(name, addedBy, location);
    }

    private static String normalizeName(String name) {
        return name.toLowerCase();
    }

    public static class Coordinate {
        public String name;
        public String addedBy;
        public Location location;
        public String messageId;

        public Coordinate(String name, String addedBy, Location location) {
            this.name = name;
            this.addedBy = addedBy;
            this.location = location;
        }

        public void setMessageId(String id) {
            this.messageId = id;
        }

        public String toMinecraftString() {
            return this.name + " | "
                    + this.addedBy + " | "
                    + this.location.getBlockX() + " "
                    + this.location.getBlockY() + " "
                    + this.location.getBlockZ();
        }

        public String toDiscordString() {
            return this.name + "\n"
                    + this.addedBy + "\n"
                    + this.location.getBlockX() + " "
                    + this.location.getBlockY() + " "
                    + this.location.getBlockZ();
        }
    }

    public static class InitializationFailedException extends Exception {
        public InitializationFailedException(String message) {
            super(message);
        }
    }

    public static class InvalidMessageException extends Exception {
        public InvalidMessageException(Message message) {
            super("Message with id " + message.getIdAsString() + " is not a valid coordinate. " +
                    "Please refrain from posting messages in the coordinate channel.");
        }
    }

    public static class DuplicateCoordinateException extends Exception {
        public DuplicateCoordinateException(String name) {
            super("Coordinate with name \"" + name + "\" already exists.");
        }
    }

    public static class CoordinateDoesNotExistException extends Exception {
        public CoordinateDoesNotExistException(String name) {
            super("Coordinate with name \"" + name + "\" does not exist.");
        }
    }

    public static class PlayerLacksPermissionException extends Exception {
        public PlayerLacksPermissionException(String name) {
            super("You cannot delete a coordinate created by another player (" + name + ").");
        }
    }
}
