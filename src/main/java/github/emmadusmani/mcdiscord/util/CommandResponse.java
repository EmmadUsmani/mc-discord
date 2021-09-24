package github.emmadusmani.mcdiscord.util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

/**
 * Util class abstracting Minecraft console/chat message responses.
 */
public class CommandResponse {

    /**
     * Neutral response.
     */
    public static void info(CommandSender sender, String message) {
        sender.sendMessage(message);
    }

    /**
     * Confirmation of a successful action.
     */
    public static void confirmation(CommandSender sender, String message) {
        sender.sendMessage(ChatColor.GREEN + message);
    }

    /**
     * Error indicating sender input command improperly.
     */
    public static void userError(CommandSender sender, String message) {
        sender.sendMessage(ChatColor.RED + message);
    }

    /**
     * Error occurring within the plugin.
     */
    public static void serverError(CommandSender sender, String message) {
        sender.sendMessage(ChatColor.DARK_RED + message);
    }


}
