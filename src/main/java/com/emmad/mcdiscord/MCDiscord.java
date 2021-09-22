package com.emmad.mcdiscord;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public final class MCDiscord extends JavaPlugin {

    @Override
    public void onEnable() {
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
