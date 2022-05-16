package de.borekking.banSystem;

import de.borekking.banSystem.command.BSCommand;
import de.borekking.banSystem.command.CommandHandler;
import de.borekking.banSystem.command.commands.other.HelpCommand;
import de.borekking.banSystem.config.ConfigHandler;
import de.borekking.banSystem.config.ConfigSetting;
import de.borekking.banSystem.discord.DiscordBot;
import de.borekking.banSystem.util.JarUtils;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeeMain extends Plugin {

    // BungeeCord Main class (also overall main class)

    // Plugin instance
    private static BungeeMain instance;

    private CommandHandler commandHandler;
    private DiscordBot discordBot;

    @Override
    public void onLoad() {
        instance = this;
        JarUtils.importJars(this, "JDA-5.0.0-alpha.11_1e25ede-withDependencies-min.jar");
    }

    @Override
    public void onEnable() {
        new ConfigHandler(); // Create new ConfigHandler to create ConfigFile and Set ConfigSettings

        BSCommand[] commands = this.createCommands();
        this.commandHandler = new CommandHandler(commands); // Load commands (discord and minecraft)

        this.registerCommands(commands);

        // ------ <Discord Bot> ------
        String token = ConfigSetting.DISCORD_TOKEN.getValueAsString(),
                guildId = ConfigSetting.DISCORD_GUILD_ID.getValueAsString();

        this.discordBot = new DiscordBot(token, guildId, commands);
        // ------ </Discord Bot> ------

        System.out.println("Loaded Discord-Minecraft-BanSystem Plugin by borekking [v. 1.0.0 - Private Beta]");
    }

    @Override
    public void onDisable() {
        if (this.discordBot != null) {
            this.discordBot.disableBot();
        }
    }

    private void registerCommands(BSCommand[] commands) {
        ProxyServer proxyServer = instance.getProxy();
        for (BSCommand command : commands) {
            proxyServer.getPluginManager().registerCommand(instance, command);
        }
    }

    private BSCommand[] createCommands() {
        return new BSCommand[] {
            new HelpCommand()
        };
    }

    public static String getPrefix() {
        return ConfigSetting.MINECRAFT_PREFIX.getValueAsColorString();
    }

    // Utility function to send a message to a minecraft player
    public static void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(new TextComponent(getPrefix() + message));
    }

    public static void shutdown() {
        instance.getProxy().stop();

        if (instance.discordBot != null) {
            instance.discordBot.disableBot();
        }
    }

    // Utility function used for errors
    public static void sendErrorMessage(String message) {
        System.out.println(message);
    }

    public CommandHandler getCommandHandler() {
        return commandHandler;
    }

    public DiscordBot getDiscordBot() {
        return discordBot;
    }

    public static BungeeMain getInstance() {
        return instance;
    }
}
