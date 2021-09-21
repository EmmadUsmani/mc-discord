package com.emmad.mcdiscord;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.bukkit.Location;

import java.util.Collection;
import java.util.HashMap;

public class CoordinateManager {
    private static final HashMap<String, Coordinate> coordMap = new HashMap<>();
    private static final String discordURL = "https://discord.com/api/webhooks/888537014268485722/Qg-8swhzjU2lmHYrfyRV-aSWBz7_Tm4KCgZz2ABlukiLQw0Lfd7ke7DQcIJJz9JEwYf3";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    public static void saveCoordinate(String name, String addedBy, Location location)
            throws DuplicateCoordinateException {
        name = CoordinateManager.normalizeName(name);
        if (CoordinateManager.coordMap.containsKey(name)) {
            throw new DuplicateCoordinateException(name);
        }

        // send http request
        try {
            OkHttpClient client = new OkHttpClient();
            ObjectMapper mapper = new ObjectMapper();

            DiscordReqBody discordReqBody = new DiscordReqBody(name + " " + addedBy);

            String jsonStr = mapper.writeValueAsString(discordReqBody);

            RequestBody body = RequestBody.create(jsonStr, JSON);
            Request request = new Request.Builder().url(CoordinateManager.discordURL)
                    .post(body).build();
            Response response = client.newCall(request).execute();
            System.out.println(response.body().byteString());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

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

    public static class DiscordReqBody {
        public String content;

        public DiscordReqBody(String content) {
            this.content = content;
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
