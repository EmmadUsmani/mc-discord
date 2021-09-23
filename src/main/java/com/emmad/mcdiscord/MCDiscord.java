package com.emmad.mcdiscord;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public final class MCDiscord extends JavaPlugin {
    private DiscordBot discordBot;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();

        try {
            this.discordBot = new DiscordBot(this);
            CoordinateManager.discordBot = discordBot;

            CoordinateManager.loadCoordinates();

            new MCDiscordCommandExecutor(this);

        } catch (Exception e) {
            getLogger().info(ChatColor.RED + e.getMessage());
            getLogger().info(ChatColor.DARK_RED + "Initialization failed, disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        if (discordBot != null) {
            discordBot.disconnect();
        }
    }

}
