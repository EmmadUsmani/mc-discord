package com.emmad.mcdiscord;

import org.bukkit.Location;

import java.util.Collection;
import java.util.HashMap;

public class CoordinateManager {
    private static final HashMap<String, Coordinate> coordMap = new HashMap<>();

    public static void saveCoordinate(String name, String addedBy, Location location) throws DuplicateCoordinateException {
        name = CoordinateManager.normalizeName(name);
        if (CoordinateManager.coordMap.containsKey(name)) {
            throw new DuplicateCoordinateException(name);
        }
        CoordinateManager.coordMap.put(name, new Coordinate(name, addedBy, location));
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
}
