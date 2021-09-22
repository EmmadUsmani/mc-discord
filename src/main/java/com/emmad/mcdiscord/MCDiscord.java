package com.emmad.mcdiscord;

import org.bukkit.plugin.java.JavaPlugin;

public final class MCDiscord extends JavaPlugin {

    @Override
    public void onEnable() {
        new MCDiscordCommandExecutor(this);
    }

    @Override
    public void onDisable() {
        DiscordBot.discordApi.disconnect();
    }

}
