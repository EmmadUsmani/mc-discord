package com.emmad.mcdiscord;

import org.bukkit.Location;

import java.util.HashMap;

public class CoordManager {
    private static final HashMap<String, Location> coordMap = new HashMap<>();

    public static void saveCoord(String name, Location location) {
        CoordManager.coordMap.put(name, location);
    }
}
