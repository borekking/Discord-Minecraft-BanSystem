package de.borekking.banSystem.command.commands.punish.punish;

import de.borekking.banSystem.BungeeMain;
import de.borekking.banSystem.command.BSStandAloneCommand;
import de.borekking.banSystem.duration.Duration;
import de.borekking.banSystem.punishment.GeneralPunishmentHandler;
import de.borekking.banSystem.punishment.Platform;
import de.borekking.banSystem.punishment.Punishment;
import de.borekking.banSystem.util.BSUtils;
import de.borekking.banSystem.util.discord.MyEmbedBuilder;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class PunishNormalCommand extends BSStandAloneCommand {

    private final Platform[] platforms;

    private final GeneralPunishmentHandler punishmentHandler;

    public PunishNormalCommand(String commandName, String commandDescription, String userOptionDescription, GeneralPunishmentHandler punishmentHandler, Platform... platforms) {
        super(commandName, commandDescription,
                // Arguments: <user> <duration> <reason>
                new OptionData(OptionType.STRING, "user", userOptionDescription).setRequired(true),
                new OptionData(OptionType.STRING, "duration", "Duration of punishment (eg. 6min)").setRequired(true),
                new OptionData(OptionType.STRING, "reason", "Reason for punishment").setRequired(true));

        this.platforms = platforms;
        this.punishmentHandler = punishmentHandler;
    }

    @Override
    public void perform(SlashCommandInteractionEvent event) {
        if (!BungeeMain.discordUserHasPermissions(event.getMember(), this.getPermission())) {
            BungeeMain.sendNoPermissionReply(event);
            return;
        }

        String user = event.getOption("user").getAsString();
        String durationStr = event.getOption("duration").getAsString();
        String reason = event.getOption("reason").getAsString();

        long userID = this.getUserID(user, "");

        if (userID < 0) {
            event.replyEmbeds(new MyEmbedBuilder().color(Color.RED).description("User ID was invalid!").build()).queue();
            return;
        }

        long durationLong;
        try {
            durationLong = Duration.getValueOf(durationStr);
        } catch (Duration.IllegalDurationException e) {
            event.replyEmbeds(new MyEmbedBuilder().color(Color.RED).description("Duration (" + durationStr + ") was invalid.").build()).queue();
            return;
        }

        net.dv8tion.jda.api.entities.User operatorDiscordUser = event.getUser();
        String operatorPlatformID = operatorDiscordUser.getId();
        long operatorID = BSUtils.getUserIDByDiscordIDAndCreateIfAbsent(operatorPlatformID, "");

        List<Punishment> punishments = this.perform(userID, operatorID, durationLong, reason);
        event.replyEmbeds(new MyEmbedBuilder().color(Color.GREEN).description("Created " + punishments.size() + " punishments and eventually deleted old ones.").build()).queue();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // Check Permission for MC users
        if (!BungeeMain.minecraftPlayerHasPermissions(sender, this.getPermission())) {
            return;
        }

        if (args.length < 3) {
            BungeeMain.sendMessage(sender, ChatColor.RED + "Too less arguments!", this.getUsage());
            return;
        }

        String user = args[0];
        String durationStr = args[1];
        String reason = args[2];

        long userID = this.getUserID(user, "");

        if (userID < 0) {
            BungeeMain.sendMessage(sender, ChatColor.RED + "User ID was invalid!");
            return;
        }

        long durationLong;
        try {
            durationLong = Duration.getValueOf(durationStr);
        } catch (Duration.IllegalDurationException e) {
            BungeeMain.sendMessage(sender, ChatColor.RED + "Duration (" + durationStr + ") was invalid.");
            return;
        }

        long operatorID = -1L;
        if (sender instanceof ProxiedPlayer) {
            String operatorPlatformID = ((ProxiedPlayer) sender).getUniqueId().toString();
            operatorID = BSUtils.getUserIDByMinecraftIDAndCreateIfAbsent(operatorPlatformID, "");
        }

        List<Punishment> punishments = this.perform(userID, operatorID, durationLong, reason);
        BungeeMain.sendMessage(sender, ChatColor.GREEN + "Created " + punishments.size() + " punishments and eventually deleted old ones.");
    }

    private List<Punishment> perform(long userID, long operatorID, long duration, String reason) {
        long timestampStart = System.currentTimeMillis();
        long timestampEnd = duration < 0L ? -1L : timestampStart + duration;
        reason = reason == null ? "" : reason;

        List<Punishment> punishments = new ArrayList<>();

        for (Platform platform : this.platforms) {
            Punishment punishment = new Punishment(userID, operatorID, timestampStart, timestampEnd, platform, reason);

            this.punishmentHandler.punish(punishment);
            punishments.add(punishment);
        }

        return punishments;
    }

    private long getUserID(String platformID, String permissionOnAbsent) {
        for (Platform platform : this.platforms) {
            if (!platform.platformIDIsValid(platformID)) continue;

            return platform.getUserIDAncCreateIfAbsent(platformID, permissionOnAbsent);
        }
        return -1L;
    }

}
