package github.emmadusmani.mcdiscord;

import github.emmadusmani.mcdiscord.coordinate.CoordinateManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;

import java.util.Optional;

/**
 * An abstraction layer over the Discord Api, using the Javacord library.
 * Handles all Discord related actions in our application.
 */
public class DiscordBot {
    private final DiscordApi api;
    private final TextChannel coordinateChannel;

    /**
     * Gets config values from user supplied config.yml and initializes api and
     * text channel.
     *
     * @param plugin the plugin itself, defined in Main
     * @throws InitializationFailedException if bot token or channel id are not
     *                                       provided or are invalid
     */
    public DiscordBot(JavaPlugin plugin) throws InitializationFailedException {
        // read config values
        FileConfiguration config = plugin.getConfig();
        String botToken = config.getString("discord-bot-token");
        if (botToken == null) {
            throw new InitializationFailedException("\"discord-bot-token\" not provided in config.yml.");
        }
        String channelId = config.getString("discord-coordinate-channel-id");
        if (channelId == null) {
            throw new InitializationFailedException("\"discord-coordinate-channel-id\" not provided in config.yml.");
        }

        // initialize bot
        try {
            this.api = new DiscordApiBuilder().setToken(botToken).login().join();
        } catch (Exception e) {
            throw new InitializationFailedException("Failed to initialize DiscordApi." +
                    " Make sure the bot token you provided is valid.", e);
        }

        // get text channel
        Optional<TextChannel> optional = api.getTextChannelById(channelId);
        if (!optional.isPresent()) {
            throw new InitializationFailedException("Coordinate text channel with provided id " +
                    channelId + " does not exist.");
        }
        this.coordinateChannel = optional.get();
    }

    /**
     * Posts a message to the text channel containing info about the coordinate,
     * as described by CoordinateManager.Coordinate#toDiscordString.
     */
    public void postCoordinateMessage(CoordinateManager.Coordinate coordinate) throws RequestFailedException {
        try {
            Message message = coordinateChannel.sendMessage(coordinate.toDiscordString()).join();
            coordinate.setMessageId(message.getIdAsString());
        } catch (Exception e) {
            throw new RequestFailedException("Failed to post coordinate to Discord.", e);
        }
    }

    /**
     * Deletes the message describing the given coordinate from the text channel.
     */
    public void deleteCoordinateMessage(CoordinateManager.Coordinate coordinate) throws RequestFailedException {
        try {
            coordinateChannel.deleteMessages(coordinate.messageId).join();
        } catch (Exception e) {
            throw new RequestFailedException("Failed to delete coordinate from Discord.", e);
        }
    }

    /**
     * Gets all Messages from the coordinate text channel.
     */
    public Message[] getCoordinateMessages() throws RequestFailedException {
        try {
            return coordinateChannel.getMessagesAsStream().toArray(Message[]::new);
        } catch (Exception e) {
            throw new RequestFailedException("Failed to get coordinates from Discord.", e);
        }
    }

    /**
     * Disconnects the DiscordApi instance. Should be called before plugin shutdown.
     */
    public void disconnect() {
        try {
            api.disconnect();
        } catch (Exception ignored) {
        }
    }

    public static class InitializationFailedException extends Exception {
        public InitializationFailedException(String message) {
            super(message);
        }

        public InitializationFailedException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class RequestFailedException extends Exception {
        public RequestFailedException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
