package com.emmad.mcdiscord;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.javacord.api.DiscordApiBuilder;

public final class MCDiscord extends JavaPlugin {

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        FileConfiguration config = this.getConfig();
        String discordBotToken = config.getString("discord-bot-token");
        String discordChannelId = config.getString("discord-coordinate-channel-id");
        if (discordBotToken == null || discordChannelId == null) {
            this.getLogger().info(ChatColor.RED + "Config not provided.");
            return;
        }

        DiscordBot.discordApi = new DiscordApiBuilder().setToken(discordBotToken).login().join();
        DiscordBot.channelID = discordChannelId;

        try {
            CoordinateManager.loadCoordinates();
        } catch (DiscordBot.RequestFailedException e) {
            this.getLogger().info(e.getMessage());
            this.getLogger().info(ChatColor.DARK_RED + "Failed to load coordinates.");
        }
        new MCDiscordCommandExecutor(this);
    }

    @Override
    public void onDisable() {
        DiscordBot.discordApi.disconnect();
    }

}
