package de.borekking.banSystem.punishment;

import de.borekking.banSystem.BungeeMain;
import de.borekking.banSystem.config.ConfigSetting;
import de.borekking.banSystem.duration.Duration;
import de.borekking.banSystem.punishment.user.User;
import de.borekking.banSystem.util.JavaUtils;
import de.borekking.banSystem.util.discord.DiscordUtils;
import de.borekking.banSystem.util.discord.MyEmbedBuilder;

import java.awt.Color;
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
            case DISCORD: {
                // Ban user from discord guild
                List<Long> discordIDs = user.getDiscordIDs();
                String reason = ConfigSetting.BAN_DISCORD_MESSAGE.getValueAsString().replace("%reason%", punishment.getReason());

                for (Long id : discordIDs) {
                    net.dv8tion.jda.api.entities.User discordUser = DiscordUtils.getUserByID(id);
                    if (discordUser == null) continue; // NPE!

                    DiscordUtils.ban(discordUser, 2, reason); // TODO delDays
                }
                break;
            } case MINECRAFT: {
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
        }
    }, punishment -> {
        // Unban
        Platform platform = punishment.getPlatform();
        User user = BungeeMain.getUser(punishment.getUserID());

        switch (platform) {
            case DISCORD: {
                // Unban user from discord guild
                List<Long> discordIDs = user.getDiscordIDs();
                for (Long id : discordIDs) {
                    net.dv8tion.jda.api.entities.User discordUser = DiscordUtils.getUserByID(id);
                    if (discordUser == null) continue; // NPE!

                    DiscordUtils.unban(discordUser);
                }
                break;
            } case MINECRAFT:
                // No Action on minecraft unban
                break;
        }
    }, punishment -> {
        // BR Ban
        long banBroadcastChannelID = ConfigSetting.BAN_BROADCAST_CHANNEL.getValueAsLong();
        User user = BungeeMain.getUser(punishment.getUserID());

        BungeeMain.getInstance().getBroadcaster().sendMessage(banBroadcastChannelID,
                new MyEmbedBuilder()
                        .color(Color.RED)
                        .title("Ban")
                        .description("Banned user " + user.getName())
                        .field(JavaUtils.createMap(
                                "Name", user.getName(),
                                "Platform", punishment.getPlatform().name(),
                                "Duration", Duration.getMessage(punishment.getDuration()),
                                "Reason", punishment.getReason()), false)
                        .build());
    }, punishment -> {
        // BR Unban
        long banBroadcastChannelID = ConfigSetting.BAN_BROADCAST_CHANNEL.getValueAsLong();
        User user = BungeeMain.getUser(punishment.getUserID());

        BungeeMain.getInstance().getBroadcaster().sendMessage(banBroadcastChannelID,
                new MyEmbedBuilder()
                        .color(Color.GREEN)
                        .title("Unban")
                        .description("Unbanned user " + user.getName())
                        .field("Name", user.getName(), false)
                        .field("Platform", punishment.getPlatform().name(), false)
                        .build());
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

                    if (!message.isEmpty()) {
                        discordUser.openPrivateChannel().queue(channel -> channel.sendMessage(message).queue());
                    }
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

                    if (!message.isEmpty()) {
                        discordUser.openPrivateChannel().queue(channel -> channel.sendMessage(message).queue());
                    }
                }
                break;
            }
            case MINECRAFT: {
                // Send message to minecraft user
                String message = ConfigSetting.MUTE_MINECRAFT_UNMUTE.getValueAsString().replace("%reason%", punishment.getReason());

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
        // BR Mute
        long muteBroadcastChannelID = ConfigSetting.MUTE_BROADCAST_CHANNEL.getValueAsLong();
        User user = BungeeMain.getUser(punishment.getUserID());

        BungeeMain.getInstance().getBroadcaster().sendMessage(muteBroadcastChannelID,
                new MyEmbedBuilder()
                        .color(Color.RED)
                        .title("Mute")
                        .description("Muted user " + user.getName())
                        .field(JavaUtils.createMap(
                                "Name", user.getName(),
                                "Platform", punishment.getPlatform().name(),
                                "Duration", Duration.getMessage(punishment.getDuration()),
                                "Reason", punishment.getReason()), false)
                        .build());
    }, punishment -> {
        // BR Unmute
        long muteBroadcastChannelID = ConfigSetting.MUTE_BROADCAST_CHANNEL.getValueAsLong();
        User user = BungeeMain.getUser(punishment.getUserID());

        BungeeMain.getInstance().getBroadcaster().sendMessage(muteBroadcastChannelID,
                new MyEmbedBuilder()
                        .color(Color.GREEN)
                        .title("Unmute")
                        .description("Unmuted user " + user.getName())
                        .field("Name", user.getName(), false)
                        .field("Platform", punishment.getPlatform().name(), false)
                        .build());
    });

    private final Consumer<Punishment> punish, unPunish, broadcastPunishment, broadcastUnPunishment;

    PunishmentType(Consumer<Punishment> onPunish, Consumer<Punishment> onUnPunish, Consumer<Punishment> broadcastPunishment, Consumer<Punishment> broadcastUnPunishment) {
        this.punish = onPunish;
        this.unPunish = onUnPunish;
        this.broadcastPunishment = broadcastPunishment;
        this.broadcastUnPunishment = broadcastUnPunishment;
    }

    public void punish(Punishment punishment) {
        this.punish.accept(punishment);
    }

    public void unPunish(Punishment punishment) {
        this.unPunish.accept(punishment);
    }

    public void broadcastPunishment(Punishment punishment) {
        this.broadcastPunishment.accept(punishment);
    }

    public void broadcastUnPunishment(Punishment punishment) {
        this.broadcastUnPunishment.accept(punishment);
    }
}
