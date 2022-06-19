package de.borekking.banSystem;

import de.borekking.banSystem.broadcast.Broadcaster;
import de.borekking.banSystem.command.BSCommand;
import de.borekking.banSystem.command.CommandBuilder;
import de.borekking.banSystem.command.CommandHandler;
import de.borekking.banSystem.command.commands.other.AutoIDCommand;
import de.borekking.banSystem.command.commands.other.DurationsCommand;
import de.borekking.banSystem.command.commands.punish.punish.PunishAutoCommand;
import de.borekking.banSystem.command.commands.punish.punish.PunishNormalCommand;
import de.borekking.banSystem.command.commands.punish.unpunish.UnpunishCommand;
import de.borekking.banSystem.command.commands.other.HelpCommand;
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

import java.util.Deque;
import java.util.LinkedList;
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
     *  0. Commands:
     *        - Durations Command +
     *        - Auto-ID Command +
     *        - Help Command +
     *        - Reload -> implement reload shit
     *  1. Broadcaster: On Mute, un-mute, ... -> PunishmentType
     *  2. Merge Command: UserManager::merge: long : userIDA, long : userIDB ->
     *     -> /merge <m/d> <userA> <m/d> <userB>
     *  3. Unmerge Command: <- Doen't make sense -> Do /remove link <platform> <user> <platformID to remove>
     *  4. OperatorID: While doing punishments, un-punishments: Get/Create User (BSUtils:getAndCreateOnAbsent) -> add to Punishment
     *  5. Permissions:
     *        - add permissions: <user> <platform> <permissions>
     *        - remove permissions: <user> <platform> <permissions>
     *        - Add Permissions to Commands as attribute -> check permissions (UserManager:getUser)
     *        - e.g. "ban.*", "ban.minecraft.auto"
     *  6. Other:
     *        - Message if user to ban has too high permission (-> permissions with rank?, -> dc: higher than the bot)
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
     *       Synced:
     *       Ban a user on all platforms.
     *          /ban synced normal <discordID/name#tag/uuid/mc-name> <duration> <reason>
     *          /ban synced auto <discordID/name#tag/uuid/mc-name> <auto-id>
     *
     *    Unban:
     *    Unban a user.
     *       MC:
     *       Unban a minecraft user.
     *          /unban minecraft <uuid/name>
     *       DC:
     *       Unban a discord user.
     *          /unban discord <id/name#tag>
     *       Synced:
     *       Unban a user on all platforms.
     *          /unban synced <discordID/name#tag/uuid/mc-name>
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
     *       Synced:
     *       Ban a user on all platforms
     *          /mute synced normal <discordID/name#tag/uuid/mc-name> <duration> <reason>
     *          /mute synced auto <discordID/name#tag/uuid/mc-name> <auto-id>
     *
     *    Unmute:
     *    Unmute a user.
     *       MC:
     *       Unmute a minecraft user.
     *          /unmute minecraft <uuid/name>
     *       DC:
     *       Unmute a discord user.
     *          /unmute discord <id/name#tag>
     *       Synced:
     *       Unmute a user on all platforms.
     *          /unmute synced <discordID/name#tag/uuid/mc-name>
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
     *    unban:
     *       minecraft: <uuid/name>
     *       discord: <id/name#tag>
     *       synced: <discordID/name#tag/uuid/mc-name>
     *
     *    unmute:
     *       minecraft: <uuid/name>
     *       discord: <id/name#tag>
     *       synced: <discordID/name#tag/uuid/mc-name>
     *
     *    -> Procedure: Get userID by Platform and PlatformID (BungeeMain),
     *                  Get Punishment from associated GPH,
     *                  Remove Punishment with associated GPH
     *    -> Abstract class using abstract Methode:String to get userID.
     *
     *
     * Every SubCommand has to be a class.
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

    private Broadcaster broadcaster;

    @Override
    public void onLoad() {
        instance = this;
        JarUtils.importJars(this, "JDA-5.0.0-alpha.11_1e25ede-withDependencies-min.jar");
        JarUtils.importJars(this, "postgresql-42.3.5.jar");
    }

    @Override
    public void onEnable() {
        new ConfigHandler(); // Create new ConfigHandler to create ConfigFile and Set ConfigSettings

        this.broadcaster = new Broadcaster();

        // Create AutoBanHandler and AutoMuteHandler
        this.autoBans = new AutoReasonHandler("autopunishments", "bans");
        this.autoMutes = new AutoReasonHandler("autopunishments", "mutes");

        // Create SQL Client (Before PunishmentHandlers!)
        this.sqlClient = new SQLClient(ConfigSetting.SQL_HOST.getValueAsString(), ConfigSetting.SQL_DATABASE.getValueAsString(),
                ConfigSetting.SQL_USER.getValueAsString(), ConfigSetting.SQL_PASSWORD.getValueAsString());

        // Create PunishmentHandlers (Before Commands!)
        this.banHandler = new GeneralPunishmentHandler(this.sqlClient, "ban", PunishmentType.BAN);
        this.muteHandler = new GeneralPunishmentHandler(this.sqlClient, "mute", PunishmentType.MUTE);

        BSCommand[] commands = this.createCommands();
        this.commandHandler = new CommandHandler(commands); // Load commands (discord and minecraft)

        this.registerCommands(commands);
        this.registerListeners();

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

        // Return true if user is admin
        switch (platform) {
            case MINECRAFT: {
                UUID uuid = Platform.getMinecraftUUID(platformID);
                ProxiedPlayer player = BungeeMain.getPlayer(uuid);
                if (player == null) return false;
                return player.getGroups().contains("admin");
            }
            case DISCORD: {
                long discordID = Platform.getDiscordID(platformID);
                net.dv8tion.jda.api.entities.User discordUser = DiscordUtils.getUserByID(discordID);
                if (discordUser == null) return false;

                Member member = DiscordUtils.getMember(BungeeMain.getInstance().getGuild(), discordUser);
                if (member == null) return false;

                return member.hasPermission(Permission.ADMINISTRATOR);
            }
        }
        return false;
    }

    // Check Permission for MC users
    public static boolean minecraftPlayerHasPermissions(CommandSender sender) {
        return !(sender instanceof ProxiedPlayer) ||
                BungeeMain.hasPermission(Platform.MINECRAFT, String.valueOf(((ProxiedPlayer) sender).getUniqueId()));
    }

    private BSCommand[] createCommands() {
        BSCommand[] commands = new BSCommand[]{
                // ----- Other -----
                new DurationsCommand(),

                new AutoIDCommand(),

                // ----- Ban -----
                new CommandBuilder("ban", "Ban users")
                        // Add sub-command-groups
                        .addSubCommandGroup("minecraft", "Ban a minecraft user")
                        .addSubCommandGroup("discord", "Ban a discord user")
                        .addSubCommandGroup("synced", "Ban user on discord and minecraft")
                        // Add SubCommands for groups
                        .addSubCommand("minecraft", new PunishNormalCommand("normal", "Ban a minecraft user (normal)",
                                "Minecraft user by uuid or username", this.banHandler, Platform.MINECRAFT))
                        .addSubCommand("minecraft", new PunishAutoCommand("auto", "Ban a minecraft user (auto))",
                                "Minecraft user by uuid or username", this.banHandler, this.autoBans, Platform.MINECRAFT))

                        .addSubCommand("discord", new PunishNormalCommand("normal", "Ban a discord user (normal)",
                                "Discord user by id or tag", this.banHandler, Platform.DISCORD))
                        .addSubCommand("discord", new PunishAutoCommand("auto", "Ban a discord user (auto))",
                                "Discord user by id or tag", this.banHandler, this.autoBans, Platform.DISCORD))

                        .addSubCommand("synced", new PunishNormalCommand("normal", "Ban a user synced (dc and mc, normal)",
                                "Discord user by id or tag", this.banHandler, Platform.DISCORD, Platform.MINECRAFT))
                        .addSubCommand("synced", new PunishAutoCommand("auto", "Ban a user synced (dc and mc, auto))",
                                "ID: Minecraft (uuid/name) or Discord (id/tag)", this.banHandler, this.autoBans, Platform.DISCORD, Platform.MINECRAFT))
                        .create(),
                // ----- Unban -----
                new CommandBuilder("unban", "Unban users")
                        .addSubCommand(new UnpunishCommand("minecraft", "Unban a minecraft user",
                                "Minecraft user by uuid or username", this.banHandler, Platform.MINECRAFT))
                        .addSubCommand(new UnpunishCommand("discord",
                                "Unban a discord user", "Discord user by id or tag",
                                this.banHandler, Platform.DISCORD))
                        .addSubCommand(new UnpunishCommand("synced", "Unban on all platforms (dc, mc)",
                                "ID: Minecraft (uuid/name) or Discord (id/tag)", this.banHandler, Platform.DISCORD, Platform.MINECRAFT))
                        .create(),
                // ----- Mute -----
                new CommandBuilder("mute", "Mute users")
                        // Add sub-command-groups
                        .addSubCommandGroup("minecraft", "Mute a minecraft user")
                        .addSubCommandGroup("discord", "Mute a discord user")
                        .addSubCommandGroup("synced", "Mute user on discord and minecraft")
                        // Add SubCommands for groups
                        .addSubCommand("minecraft", new PunishNormalCommand("normal", "Mute a minecraft user (normal)",
                                "Minecraft user by uuid or username", this.muteHandler, Platform.MINECRAFT))
                        .addSubCommand("minecraft", new PunishAutoCommand("auto", "Mute a minecraft user (auto))",
                                "Minecraft user by uuid or username", this.muteHandler, this.autoMutes, Platform.MINECRAFT))

                        .addSubCommand("discord", new PunishNormalCommand("normal", "Mute a discord user (normal)",
                                "Discord user by id or tag", this.muteHandler, Platform.DISCORD))
                        .addSubCommand("discord", new PunishAutoCommand("auto", "Mute a discord user (auto)",
                                "Discord user by id or tag", this.muteHandler, this.autoMutes, Platform.DISCORD))

                        .addSubCommand("synced", new PunishNormalCommand("normal", "Mute a user synced (dc and mc, normal)",
                                "Discord user by id or tag", this.muteHandler, Platform.DISCORD, Platform.MINECRAFT))
                        .addSubCommand("synced", new PunishAutoCommand("auto", "Mute a user synced (dc and mc, auto)",
                                "ID: Minecraft (uuid/name) or Discord (id/tag)", this.muteHandler, this.autoMutes, Platform.DISCORD, Platform.MINECRAFT))
                        .create(),

                // ----- Unmute -----
                new CommandBuilder("unmute", "Unmute users")
                        .addSubCommand(new UnpunishCommand("minecraft", "Unmute a minecraft user",
                                "Minecraft user by uuid or username", this.muteHandler, Platform.MINECRAFT))
                        .addSubCommand(new UnpunishCommand("discord",
                                "Unmute a discord user", "Discord user by id or tag",
                                this.muteHandler, Platform.DISCORD))
                        .addSubCommand(new UnpunishCommand("synced", "Unmute on all platforms (dc, mc)",
                                "ID: Minecraft (uuid/name) or Discord (id/tag)", this.muteHandler, Platform.DISCORD, Platform.MINECRAFT))
                        .create()
        };

        return JavaUtils.mergeArrays(BSCommand[]::new, commands, new BSCommand[] {new HelpCommand(commands)});
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
        sendMessage(sender, new String[] {message1}, messages);
    }

    public static void sendMessage(CommandSender sender, String[]... messages) {
        String line = "-------------------------";

        // List w/ messages
        Deque<String> messageList = new LinkedList<>(JavaUtils.getAsList(JavaUtils.mergeArrays(messages)));
        messageList.addLast(line);
        messageList.addFirst(line);

        // Create TextComponent
        TextComponent textComponent;

        // Send either text with prefix (players) or without (else)
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

    public Guild getGuild() {
        return discordBot.getGuild();
    }

    public static BungeeMain getInstance() {
        return instance;
    }

    public Broadcaster getBroadcaster() {
        return broadcaster;
    }
}
