package com.emmad.mcdiscord;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DiscordBot {
    private static final String channelID = "888536979338313731";

    public static final DiscordApi discordApi = new DiscordApiBuilder()
            .setToken("ODkwMjU2MDYyMTY1NDE4MDA1.YUtJkQ.DmAfk1sZYRYFxtq_PUl_6HhrlJM")
            .login().join();

    public static void postCoordinate(CoordinateManager.Coordinate coordinate) throws RequestFailedException {
        try {
            Optional<TextChannel> optionalTextChannel = DiscordBot.discordApi.getTextChannelById(channelID);
            if (!optionalTextChannel.isPresent()) {
                throw new Exception("Text channel with provided id " + channelID + "does not exist.");
            }
            TextChannel textChannel = optionalTextChannel.get();

            textChannel.sendMessage(coordinate.toDiscordString()).join();
        } catch (Exception e) {
            throw new RequestFailedException(e.getMessage());
        }
    }

    public static List<String> getCoordinateMessages() throws RequestFailedException {
        try {
            Optional<TextChannel> optionalTextChannel = DiscordBot.discordApi.getTextChannelById(channelID);
            if (!optionalTextChannel.isPresent()) {
                throw new Exception("Text channel with provided id " + channelID + "does not exist.");
            }
            TextChannel textChannel = optionalTextChannel.get();

            Message[] messages = textChannel.getMessagesAsStream().toArray(Message[]::new);
            List<String> msgContentList = new ArrayList<>();
            for (Message message : messages) {
                msgContentList.add(message.getContent());
            }

            return msgContentList;
        } catch (Exception e) {
            throw new RequestFailedException(e.getMessage());
        }
    }

    public static class RequestFailedException extends Exception {
        public RequestFailedException(String message) {
            super(message);
        }
    }
}
