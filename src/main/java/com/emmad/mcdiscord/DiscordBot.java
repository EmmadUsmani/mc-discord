package com.emmad.mcdiscord;

import com.emmad.mcdiscord.coordinate.CoordinateManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;

import java.util.Optional;

public class DiscordBot {
    private final DiscordApi api;
    private final TextChannel coordinateChannel;

    public DiscordBot(JavaPlugin plugin) throws InitializationFailedException {
        FileConfiguration config = plugin.getConfig();
        String botToken = config.getString("discord-bot-token");
        if (botToken == null) {
            throw new InitializationFailedException("\"discord-bot-token\" not provided in config.yml.");
        }
        String channelId = config.getString("discord-coordinate-channel-id");
        if (channelId == null) {
            throw new InitializationFailedException("\"discord-coordinate-channel-id\" not provided in config.yml.");
        }

        try {
            this.api = new DiscordApiBuilder().setToken(botToken).login().join();
        } catch (Exception e) {
            throw new InitializationFailedException("Failed to initialize DiscordApi." +
                    " Make sure the bot token you provided is valid.");
        }

        Optional<TextChannel> optional = api.getTextChannelById(channelId);
        if (!optional.isPresent()) {
            throw new InitializationFailedException("Coordinate text channel with provided id "
                    + channelId + " does not exist.");
        }
        this.coordinateChannel = optional.get();
    }

    public void postCoordinateMessage(CoordinateManager.Coordinate coordinate) throws RequestFailedException {
        try {
            Message message = coordinateChannel.sendMessage(coordinate.toDiscordString()).join();
            coordinate.setMessageId(message.getIdAsString());
        } catch (Exception e) {
            throw new RequestFailedException(e.getMessage());
        }
    }

    public void deleteCoordinateMessage(CoordinateManager.Coordinate coordinate) throws RequestFailedException {
        try {
            coordinateChannel.deleteMessages(coordinate.messageId).join();
        } catch (Exception e) {
            throw new RequestFailedException(e.getMessage());
        }
    }

    public Message[] getCoordinateMessages() throws RequestFailedException {
        try {
            return coordinateChannel.getMessagesAsStream().toArray(Message[]::new);
        } catch (Exception e) {
            throw new RequestFailedException(e.getMessage());
        }
    }

    public void disconnect() {
        try {
            api.disconnect();
        } catch (Exception ignored) {}
    }

    public static class InitializationFailedException extends Exception {
        public InitializationFailedException(String message) {
            super(message);
        }
    }

    public static class RequestFailedException extends Exception {
        public RequestFailedException(String message) {
            super(message);
        }
    }
}
