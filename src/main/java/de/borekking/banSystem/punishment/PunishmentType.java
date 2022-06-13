package de.borekking.banSystem.punishment;

import de.borekking.banSystem.BungeeMain;
import de.borekking.banSystem.config.ConfigSetting;
import de.borekking.banSystem.punishment.user.User;
import de.borekking.banSystem.util.discord.DiscordUtils;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import net.dv8tion.jda.api.entities.Role;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public enum PunishmentType {

    BAN(punishment -> {
        // Ban
        Platform platform = punishment.getPlatform();
        User user = BungeeMain.getUser(punishment.getUserID());
        switch (platform) {
            case DISCORD:
                // Ban user from discord guild
                List<Long> discordIDs = user.getDiscordIDs();
                for (Long id : discordIDs) {
                    net.dv8tion.jda.api.entities.User discordUser = DiscordUtils.getUserByID(id);
                    if (discordUser == null) continue; // NPE!

                    String reason = ConfigSetting.BAN_DISCORD_MESSAGE.getValueAsString().replace("%reason%", punishment.getReason());
                    DiscordUtils.ban(discordUser, 2, reason); // TODO delDays
                }
                break;
            case MINECRAFT:
                // Kick user from Server (MC)
                List<UUID> uuids = user.getUuids();
                String reason = ConfigSetting.BAN_MINECRAFT_MESSAGE.getValueAsString().replace("%reason%", punishment.getReason());
                for (UUID uuid : uuids) {
                    ProxiedPlayer player = BungeeMain.getPlayer(uuid);
                    if (player == null) continue; // NPE!

                    player.disconnect(new TextComponent(reason));
                }
                break;
        }
    }, punishment -> {
        // Unban
        Platform platform = punishment.getPlatform();
        User user = BungeeMain.getUser(punishment.getUserID());
        switch (platform) {
            case DISCORD:
                // Unban user from discord guild
                List<Long> discordIDs = user.getDiscordIDs();
                for (Long id : discordIDs) {
                    net.dv8tion.jda.api.entities.User discordUser = DiscordUtils.getUserByID(id);
                    if (discordUser == null) continue; // NPE!

                    DiscordUtils.unban(discordUser);
                }
                break;
            case MINECRAFT:
                // No Action on minecraft unban
                break;
        }
    }),
    MUTE(punishment -> {
        // Mute
        Platform platform = punishment.getPlatform();
        User user = BungeeMain.getUser(punishment.getUserID());
        switch (platform) {
            case DISCORD: {
                Role muteRole = BungeeMain.getMuteRole();
                if (muteRole == null) break; // TODO - MSG?

                String message = ConfigSetting.MUTE_DISCORD_MUTE.getValueAsString().replace("%reason%", punishment.getReason());

                // Add mute role to user and send DM
                List<Long> discordIDs = user.getDiscordIDs();
                for (Long id : discordIDs) {
                    net.dv8tion.jda.api.entities.User discordUser = DiscordUtils.getUserByID(id);
                    if (discordUser == null) continue; // NPE!

                    DiscordUtils.addRole(discordUser, muteRole);

                    discordUser.openPrivateChannel().queue(channel -> channel.sendMessage(message).queue());
                }
                break;
            }
            case MINECRAFT: {
                // Send message to minecraft user
                String message = ConfigSetting.MUTE_MINECRAFT_MUTE.getValueAsString().replace("%reason%", punishment.getReason());

                List<UUID> uuids = user.getUuids();
                for (UUID uuid : uuids) {
                    ProxiedPlayer player = BungeeMain.getPlayer(uuid);
                    if (player == null) continue; // NPE!

                    BungeeMain.sendMessage(player, message);
                }
                break;
            }
        }
    }, punishment -> {
        // Unmute
        Platform platform = punishment.getPlatform();
        User user = BungeeMain.getUser(punishment.getUserID());
        switch (platform) {
            case DISCORD: {
                // Remove mute role
                Role muteRole = BungeeMain.getMuteRole();
                if (muteRole == null) break; // TODO - MSG?

                String message = ConfigSetting.MUTE_DISCORD_UNMUTE.getValueAsString().replace("%reason%", punishment.getReason());

                // Add mute role to user and send DM
                List<Long> discordIDs = user.getDiscordIDs();
                for (Long id : discordIDs) {
                    net.dv8tion.jda.api.entities.User discordUser = DiscordUtils.getUserByID(id);
                    if (discordUser == null) continue; // NPE!

                    DiscordUtils.addRole(discordUser, muteRole);
                    discordUser.openPrivateChannel().queue(channel -> channel.sendMessage(message).queue());
                }
                break;
            }
            case MINECRAFT: {
                // Send message to minecraft user
                String message = ConfigSetting.MUTE_MINECRAFT_MUTE.getValueAsString().replace("%reason%", punishment.getReason());

                List<UUID> uuids = user.getUuids();
                for (UUID uuid : uuids) {
                    ProxiedPlayer player = BungeeMain.getPlayer(uuid);
                    if (player == null) continue; // NPE!

                    BungeeMain.sendMessage(player, message);
                }
                break;
            }
        }
    });

    private final Consumer<Punishment> onPunish, onUnPunish;

    PunishmentType(Consumer<Punishment> onPunish, Consumer<Punishment> onUnPunish) {
        this.onPunish = onPunish;
        this.onUnPunish = onUnPunish;
    }

    public void handlePunishment(Punishment punishment) {
        this.onPunish.accept(punishment);
    }

    public void handleUnPunish(Punishment punishment) {
        this.onUnPunish.accept(punishment);
    }

}
