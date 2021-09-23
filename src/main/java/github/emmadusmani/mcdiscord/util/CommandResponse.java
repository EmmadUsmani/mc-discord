package github.emmadusmani.mcdiscord.util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class CommandResponse {

    public static void info(CommandSender sender, String message) {
        sender.sendMessage(message);
    }

    public static void confirmation(CommandSender sender, String message) {
        sender.sendMessage(ChatColor.GREEN + message);
    }

    public static void userError(CommandSender sender, String message) {
        sender.sendMessage(ChatColor.RED + message);
    }

    public static void serverError(CommandSender sender, String message) {
        sender.sendMessage(ChatColor.DARK_RED + message);
    }


}
