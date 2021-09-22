package com.emmad.mcdiscord;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class CoordinateManager {
    private static final HashMap<String, Coordinate> coordMap = new HashMap<>();

    public static void loadCoordinates() throws DiscordBot.RequestFailedException {
        List<String> coordinateMessages = DiscordBot.getCoordinateMessages();

        for (String message : coordinateMessages) {
            try {
                Coordinate coordinate = deserializeCoordinate(message);
                if (coordMap.containsKey(coordinate.name)) {
                    throw new DuplicateCoordinateException(coordinate.name);
                } else {
                    coordMap.put(coordinate.name, coordinate);
                }
            } catch (Exception e) {
                System.out.println(e.getMessage()); // TODO: use plugin's logger instead
            }
        }
    }

    public static void saveCoordinate(String name, String addedBy, Location location)
            throws DuplicateCoordinateException, DiscordBot.RequestFailedException {
        name = CoordinateManager.normalizeName(name);
        if (CoordinateManager.coordMap.containsKey(name)) {
            throw new DuplicateCoordinateException(name);
        }

        Coordinate coord = new Coordinate(name, addedBy, location);
        DiscordBot.postCoordinate(coord);
        CoordinateManager.coordMap.put(name, new Coordinate(name, addedBy, location));
    }

    public static void deleteCoordinate(String name, String playerName)
            throws CoordinateDoesNotExistException, PlayerLacksPermissionException {
        name = CoordinateManager.normalizeName(name);
        if (!CoordinateManager.coordMap.containsKey(name)) {
            throw new CoordinateDoesNotExistException(name);
        }

        Coordinate coord = CoordinateManager.coordMap.get(name);
        if (!playerName.equals(coord.addedBy)) {
            throw new PlayerLacksPermissionException(coord.addedBy);
        }

        CoordinateManager.coordMap.remove(name);
    }

    public static Collection<Coordinate> getCoordinates() {
        return CoordinateManager.coordMap.values();
    }

    public static Coordinate getCoordinate(String name) throws CoordinateDoesNotExistException {
        name = CoordinateManager.normalizeName(name);
        if (!CoordinateManager.coordMap.containsKey(name)) {
            throw new CoordinateDoesNotExistException(name);
        }
        return CoordinateManager.coordMap.get(name);
    }

    private static Coordinate deserializeCoordinate(String message) throws InvalidMessageException {
        String[] lines = message.split("\n");
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
        return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
    }

    public static class Coordinate {
        public String name;
        public String addedBy;
        public Location location;

        public Coordinate(String name, String addedBy, Location location) {
            this.name = name;
            this.addedBy = addedBy;
            this.location = location;
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

    public static class InvalidMessageException extends Exception {
        public InvalidMessageException(String coordMsg) {
            super("Message: " + coordMsg + "\nis not a valid coordinate.");
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
            super("You cannot a delete coordinate created by another player (" + name + ").");
        }
    }
}
