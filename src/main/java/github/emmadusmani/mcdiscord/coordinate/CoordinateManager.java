package github.emmadusmani.mcdiscord.coordinate;

import github.emmadusmani.mcdiscord.DiscordBot;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.javacord.api.entity.message.Message;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Global coordinate state manager. Handles coordinate operations in memory and
 * executes api calls via DiscordBot.
 */
public class CoordinateManager {
    private final JavaPlugin plugin;
    private final DiscordBot discordBot;
    private final Map<String, Coordinate> coordinateMap;

    /**
     * Populates coordinate data structure in memory.
     *
     * @param plugin     the plugin itself, defined in Main
     * @param discordBot global DiscordBot instance for the program
     * @throws InitializationFailedException if populating coordinates from Discord fails
     */
    public CoordinateManager(JavaPlugin plugin, DiscordBot discordBot) throws InitializationFailedException {
        this.plugin = plugin;
        this.discordBot = discordBot;
        this.coordinateMap = new HashMap<>();

        try {
            loadCoordinates();
        } catch (DiscordBot.RequestFailedException e) {
            throw new InitializationFailedException("Failed to load coordinates from Discord.", e);
        }
    }

    /**
     * Gets messages from Discord, then deserializes and loads coordinates into memory.
     */
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
            } catch (InvalidMessageException | DuplicateCoordinateException e) {
                plugin.getLogger().warning(e.getMessage());
            }
        }
    }

    /**
     * Saves a new Coordinate in memory and posts in Discord channel.
     */
    public void saveCoordinate(String coordinateName, String addedBy, Location location)
            throws DuplicateCoordinateException, DiscordBot.RequestFailedException {
        // check if name already exists
        coordinateName = normalizeName(coordinateName);
        if (coordinateMap.containsKey(coordinateName)) {
            throw new DuplicateCoordinateException(coordinateName);
        }

        // save
        Coordinate coordinate = new Coordinate(coordinateName, addedBy, location);
        discordBot.postCoordinateMessage(coordinate);
        coordinateMap.put(coordinateName, coordinate);
    }

    /**
     * Removes Coordinate from memory and Discord channel.
     */
    public void deleteCoordinate(String coordinateName, String playerName)
            throws CoordinateDoesNotExistException, PlayerLacksPermissionException, DiscordBot.RequestFailedException {
        // check if name doesn't exist
        coordinateName = normalizeName(coordinateName);
        if (!coordinateMap.containsKey(coordinateName)) {
            throw new CoordinateDoesNotExistException(coordinateName);
        }
        // check if player issuing command created the coordinate
        Coordinate coordinate = coordinateMap.get(coordinateName);
        if (!playerName.equals(coordinate.addedBy)) {
            throw new PlayerLacksPermissionException(coordinate.addedBy);
        }

        // delete
        discordBot.deleteCoordinateMessage(coordinate);
        coordinateMap.remove(coordinateName);
    }

    /**
     * Gets all Coordinates that have been saved.
     */
    public Collection<Coordinate> getCoordinates() {
        return coordinateMap.values();
    }

    /**
     * Gets Coordinate with given name.
     */
    public Coordinate getCoordinate(String coordinateName) throws CoordinateDoesNotExistException {
        // check if name doesn't exist
        coordinateName = normalizeName(coordinateName);
        if (!coordinateMap.containsKey(coordinateName)) {
            throw new CoordinateDoesNotExistException(coordinateName);
        }

        return coordinateMap.get(coordinateName);
    }

    /**
     * Deserializes a Discord Message describing a coordinate into a Coordinate object.
     */
    private static Coordinate deserializeCoordinate(Message message) throws InvalidMessageException {
        // get lines
        String messageContent = message.getContent();
        String[] lines = messageContent.split("\n");
        if (lines.length != 3) {
            throw new InvalidMessageException(message);
        }

        String name = lines[0];
        String addedBy = lines[1];

        // parse location from third line
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

    /**
     * Normalizes user provided coordinate name into a standard format.
     */
    private static String normalizeName(String coordinateName) {
        return coordinateName.toLowerCase();
    }

    /**
     * Object representing a coordinate in memory.
     */
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

        /**
         * Sets the id of the message associated with this coordinate in Discord.
         */
        public void setMessageId(String id) {
            this.messageId = id;
        }

        /**
         * Serializes coordinate to display in Minecraft.
         */
        public String toMinecraftString() {
            return this.name + " | "
                    + this.addedBy + " | "
                    + this.location.getBlockX() + " "
                    + this.location.getBlockY() + " "
                    + this.location.getBlockZ();
        }

        /**
         * Serializes coordinate to post to Discord.
         */
        public String toDiscordString() {
            return this.name + "\n"
                    + this.addedBy + "\n"
                    + this.location.getBlockX() + " "
                    + this.location.getBlockY() + " "
                    + this.location.getBlockZ();
        }
    }

    public static class InitializationFailedException extends Exception {
        public InitializationFailedException(String message, Throwable cause) {
            super(message, cause);
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
