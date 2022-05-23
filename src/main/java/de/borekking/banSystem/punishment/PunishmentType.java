package de.borekking.banSystem.punishment;

import de.borekking.banSystem.BungeeMain;
import de.borekking.banSystem.config.ConfigSetting;
import de.borekking.banSystem.util.discord.DiscordUtils;

import java.util.function.Consumer;

import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

public enum PunishmentType {

    BAN(punishment -> {
        Platform platform = punishment.getPlatform();
        switch (platform) {
            case DISCORD:
                // Ban user from discord guild
                User discordUser = DiscordUtils.getUser(punishment.getUserID());
                String reason = ConfigSetting.BAN_DISCORD_MESSAGE.getValueAsString().replace("%reason%", punishment.getReason());
                DiscordUtils.ban(discordUser, 2, reason);
                break;
            case MINECRAFT:
                // No Action on minecraft ban
                break;
        }
    }, punishment -> {
        Platform platform = punishment.getPlatform();
        switch (platform) {
            case DISCORD:
                // Unban user from discord
                User discordUser = DiscordUtils.getUser(punishment.getUserID());
                DiscordUtils.unban(discordUser);
                break;
            case MINECRAFT:
                // No Action on minecraft unban
                break;
        }
    }),
    MUTE(punishment -> {
        Platform platform = punishment.getPlatform();
        switch (platform) {
            case DISCORD:
                // Add mute role
                User discordUser = DiscordUtils.getUser(punishment.getUserID());
                Role muteRole = BungeeMain.getMuteRole();
                if (muteRole == null) break; // TODO - MSG?

                DiscordUtils.addRole(discordUser, muteRole);
                break;
            case MINECRAFT:
                // No action on minecraft mute
                break;
        }
    }, punishment -> {
        Platform platform = punishment.getPlatform();
        switch (platform) {
            case DISCORD:
                // Remove mute role
                User discordUser = DiscordUtils.getUser(punishment.getUserID());
                Role muteRole = BungeeMain.getMuteRole();
                if (muteRole == null) break; // TODO - MSG?

                DiscordUtils.removeRole(discordUser, muteRole);
                break;
            case MINECRAFT:
                // No action on minecraft unmute
                break;
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
