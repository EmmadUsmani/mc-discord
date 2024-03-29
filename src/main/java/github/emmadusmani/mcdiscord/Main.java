package github.emmadusmani.mcdiscord;

import github.emmadusmani.mcdiscord.coordinate.CoordinateCommandExecutor;
import github.emmadusmani.mcdiscord.coordinate.CoordinateManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {
    private DiscordBot discordBot;

    /**
     * Called when server loads plugin on startup.
     */
    @Override
    public void onEnable() {
        this.saveDefaultConfig();

        try {
            this.discordBot = new DiscordBot(this);
            CoordinateManager coordinateManager = new CoordinateManager(this, discordBot);
            new CoordinateCommandExecutor(this, coordinateManager);
        } catch (Exception e) {
            getLogger().severe(e.getMessage());
            getLogger().severe("Initialization failed, disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    /**
     * Called when server disables plugin on shutdown.
     */
    @Override
    public void onDisable() {
        if (discordBot != null) {
            discordBot.disconnect();
        }
    }

}
