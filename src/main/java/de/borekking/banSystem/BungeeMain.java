package de.borekking.banSystem;

import de.borekking.banSystem.command.BSCommand;
import de.borekking.banSystem.command.CommandBuilder;
import de.borekking.banSystem.command.CommandHandler;
import de.borekking.banSystem.command.commands.other.HelpCommand;
import de.borekking.banSystem.command.commands.ban.BanMinecraftCommand;
import de.borekking.banSystem.config.ConfigHandler;
import de.borekking.banSystem.config.ConfigSetting;
import de.borekking.banSystem.config.autoReason.AutoReasonHandler;
import de.borekking.banSystem.discord.DiscordBot;
import de.borekking.banSystem.minecraft.listener.ChatListener;
import de.borekking.banSystem.minecraft.listener.PostLoginListener;
import de.borekking.banSystem.punishment.GeneralPunishmentHandler;
import de.borekking.banSystem.punishment.Platform;
import de.borekking.banSystem.punishment.PunishmentType;
import de.borekking.banSystem.punishment.user.User;
import de.borekking.banSystem.punishment.user.UserManager;
import de.borekking.banSystem.sql.SQLClient;
import de.borekking.banSystem.util.JarUtils;
import de.borekking.banSystem.util.JavaUtils;
import de.borekking.banSystem.util.discord.DiscordUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeeMain extends Plugin {

    /*
     * TODO
     *  1. SQL +
     *  2. Punishments +
     *  3. Commands xd (Auto, normal, ...)
     *     -> Reload: Settings/Config
     *  3.2 Add Discord Broadcast on mute, unmute, ban unban.
     *  4. Permissions
     *  5. Operators (OperatorID) <- w/ permissions
     *
     */

    /*
     * Command System:
     *    Ban:
     *    Ban a user.
     *       MC:
     *       Ban a minecraft user.
     *          /ban minecraft normal <uuid/name> <duration> <reason>
     *          /ban minecraft auto <uuid/name> <auto-id>
     *       DC:
     *       Ban a discord user
     *          /ban discord normal <id/name#tag> <duration> <reason>
     *          /ban discord auto <id/name#tag> <auto-id>
     *       All Platforms:
     *       Ban a user on all platforms
     *          /ban synced normal <discordID/name#tag/uuid/mc-name> <duration> <reason>
     *          /ban synced auto <discordID/name#tag/uuid/mc-name> <auto-id>
     *
     *    Mute:
     *    Mute a user.
     *       MC:
     *       Mute a minecraft user.
     *          /mute minecraft normal <uuid/name> <duration> <reason>
     *          /mute minecraft auto <uuid/name> <auto-id>
     *       DC:
     *       Mute a discord user.
     *          /mute discord normal <id/name#tag> <duration> <reason>
     *          /mute discord auto <id/name#tag> <auto-id>
     *       All Platforms:
     *       Ban a user on all platforms
     *          /mute synced normal <discordID/name#tag/uuid/mc-name> <duration> <reason>
     *          /mute synced auto <discordID/name#tag/uuid/mc-name> <auto-id>
     *
     * For all:
     *    normal -> custom duration and reason
     *    auto -> auto duration and reason
     *
     * As Command-Hierarchy:
     *    ban:
     *       minecraft:
     *          normal: <uuid/name> <duration> <reason>
     *          auto: <uuid/name> <auto-id>
     *       discord:
     *          normal: <id/name#tag> <duration> <reason>
     *          auto: <id/name#tag> <auto-id>
     *       synced:
     *          normal: <discordID/name#tag/uuid/mc-name> <duration> <reason>
     *          auto: <discordID/name#tag/uuid/mc-name> <auto-id>
     *
     *    mute:
     *       minecraft:
     *          normal: <uuid/name> <duration> <reason>
     *          auto: <uuid/name> <auto-id>
     *       discord:
     *          normal: <id/name#tag> <duration> <reason>
     *          auto: <id/name#tag> <auto-id>
     *       synced:
     *          normal: <discordID/name#tag/uuid/mc-name> <duration> <reason>
     *          auto: <discordID/name#tag/uuid/mc-name> <auto-id>
     *
     *
     */

    // BungeeCord Main class (also overall main class)

    // Plugin instance
    private static BungeeMain instance;

    private CommandHandler commandHandler;
    private DiscordBot discordBot;

    private SQLClient sqlClient;

    private GeneralPunishmentHandler banHandler, muteHandler;

    private UserManager userManager;

    private AutoReasonHandler autoBans, autoMutes;

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
        this.registerListeners();

        // Create SQL Client
        this.sqlClient = new SQLClient(ConfigSetting.SQL_HOST.getValueAsString(), ConfigSetting.SQL_DATABASE.getValueAsString(),
                ConfigSetting.SQL_USER.getValueAsString(), ConfigSetting.SQL_PASSWORD.getValueAsString());

        // Create PunishmentHandlers
        this.banHandler = new GeneralPunishmentHandler(this.sqlClient, "ban", PunishmentType.BAN);
        this.muteHandler = new GeneralPunishmentHandler(this.sqlClient, "mute", PunishmentType.MUTE);

        // Create AutoBanHandler and AutoMuteHandler
        this.autoBans = new AutoReasonHandler("autopunishments", "bans");
        this.autoMutes = new AutoReasonHandler("autopunishments", "mutes");

        // Create User Manager
        this.userManager = new UserManager(this.sqlClient);

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

    private void registerListeners() {
        this.registerListener(new ChatListener());
        this.registerListener(new PostLoginListener());
    }

    private void registerListener(Listener listener) {
        instance.getProxy().getPluginManager().registerListener(this, listener);
    }

    public static boolean hasPermission(Platform platform, String platformID) {
        // TODO Permission System
//        long userID = getUserID(platform, platformID);
//        if (userID < 0) return false;
//
//        User user = getUser(userID);
//        if (user == null) return false;
//
//        return user.hasPermissions(permission);

        // Return true if user
        switch(platform) {
            case MINECRAFT: {
                UUID uuid = Platform.getMinecraftUUID(platformID);
                ProxiedPlayer player = BungeeMain.getPlayer(uuid);
                if (player == null) return false;
                return player.getGroups().contains("admin");
            }
            case DISCORD: {
                long discordID = Platform.getDiscordID(platformID);
                net.dv8tion.jda.api.entities.User discordUser = DiscordUtils.getUser(discordID);
                if (discordUser == null) return false;

                Member member = DiscordUtils.getMember(BungeeMain.getInstance().getGuild(), discordUser);
                if (member == null) return false;

                return member.hasPermission(Permission.ADMINISTRATOR);
            }
        }
        return false;
    }

    private BSCommand[] createCommands() {
        return new BSCommand[]{
                new HelpCommand(),
                new CommandBuilder("ban", "Ban users").addSubCommand(new BanMinecraftCommand()).create()
        };
    }

    public static Role getMuteRole() {
        long roleID = ConfigSetting.MUTE_ROLE.getValueAsLong();
        if (roleID < 0) return null;

        return DiscordUtils.getRole(roleID);
    }

    public static String getPrefix() {
        return ConfigSetting.MINECRAFT_PREFIX.getValueAsColorString();
    }

    // Utility function to send a message to a minecraft player
    public static void sendMessage(CommandSender sender, String message1, String... messages) {
        // List w/ messages
        List<String> messageList = new ArrayList<>();
        messageList.add(message1);
        messageList.addAll(JavaUtils.getAsList(messages));

        // Create TextComponent
        TextComponent textComponent;

        if (sender instanceof ProxiedPlayer) {
            textComponent = new TextComponent(getPrefix() + JavaUtils.getTextWithDelimiter(messageList, "\n" + getPrefix()));
        } else {
            textComponent = new TextComponent(JavaUtils.getTextWithDelimiter(messageList, System.lineSeparator()));
        }

        // Send message
        sender.sendMessage(textComponent);
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

    public static ProxiedPlayer getPlayer(UUID uuid) {
        return instance.getProxy().getPlayer(uuid);
    }

    public static ProxiedPlayer getPlayer(String player) {
        return instance.getProxy().getPlayer(player);
    }

    public static User getUser(long id) {
        return instance.getUserManager().getUser(id);
    }

    public static long getUserID(Platform platform, String platformID) {
        return instance.getUserManager().getUserID(platform, platformID);
    }

    public CommandHandler getCommandHandler() {
        return commandHandler;
    }

    public DiscordBot getDiscordBot() {
        return discordBot;
    }

    public SQLClient getSqlClient() {
        return sqlClient;
    }

    public GeneralPunishmentHandler getBanHandler() {
        return banHandler;
    }

    public GeneralPunishmentHandler getMuteHandler() {
        return muteHandler;
    }

    public AutoReasonHandler getAutoBans() {
        return autoBans;
    }

    public AutoReasonHandler getAutoMutes() {
        return autoMutes;
    }

    public UserManager getUserManager() {
        return userManager;
    }

    public static BungeeMain getInstance() {
        return instance;
    }
}
