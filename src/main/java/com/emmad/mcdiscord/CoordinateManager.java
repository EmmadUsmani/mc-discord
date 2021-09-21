package com.emmad.mcdiscord;

import org.bukkit.Location;

import java.util.Collection;
import java.util.HashMap;

public class CoordinateManager {
    private static final HashMap<String, Coordinate> coordMap = new HashMap<>();

    public static void saveCoordinate(String name, String addedBy, Location location) {
        CoordinateManager.coordMap.put(name, new Coordinate(name, addedBy, location));
    }

    public static Collection<Coordinate> getCoordinates() {
        return CoordinateManager.coordMap.values();
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
}
