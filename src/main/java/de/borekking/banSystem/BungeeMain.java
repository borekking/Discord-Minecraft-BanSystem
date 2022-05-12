package de.borekking.banSystem;

import de.borekking.banSystem.config.ConfigHandler;
import de.borekking.banSystem.config.ConfigSetting;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeeMain extends Plugin {

    // BungeeCord Main class

    // Plugin instance
    private static BungeeMain instance;

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        new ConfigHandler(); // Create new ConfigHandler to create ConfigFile and Set ConfigSettings

        System.out.println("Loaded Discord-Minecraft-BanSystem Plugin by borekking [v. 1.0.0 - Private Beta]");
    }

    @Override
    public void onDisable() {

    }

    public static String getPrefix() {
        return ConfigSetting.MINECRAFT_PREFIX.getValueAsColorString();
    }

    // Utility function to send a message to a minecraft player
    public static void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(new TextComponent(getPrefix() + message));
    }

    // Utility function used for errors
    public static void sendErrorMessage(String message) {
        System.out.println(message);
    }

    public static BungeeMain getInstance() {
        return instance;
    }
}
