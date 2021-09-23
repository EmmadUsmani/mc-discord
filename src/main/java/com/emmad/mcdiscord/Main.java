package com.emmad.mcdiscord;

import com.emmad.mcdiscord.coordinate.CoordinateCommandExecutor;
import com.emmad.mcdiscord.coordinate.CoordinateManager;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {
    private DiscordBot discordBot;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();

        try {
            this.discordBot = new DiscordBot(this);
            CoordinateManager coordinateManager = new CoordinateManager(this, discordBot);
            new CoordinateCommandExecutor(this, coordinateManager);
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
