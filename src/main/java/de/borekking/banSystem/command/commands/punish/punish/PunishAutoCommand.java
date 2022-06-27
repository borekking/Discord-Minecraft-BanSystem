package de.borekking.banSystem.command.commands.punish.punish;

import de.borekking.banSystem.BungeeMain;
import de.borekking.banSystem.command.BSStandAloneCommand;
import de.borekking.banSystem.config.autoReason.AutoReason;
import de.borekking.banSystem.config.autoReason.AutoReasonHandler;
import de.borekking.banSystem.duration.Duration;
import de.borekking.banSystem.punishment.GeneralPunishmentHandler;
import de.borekking.banSystem.punishment.Platform;
import de.borekking.banSystem.punishment.Punishment;
import de.borekking.banSystem.util.BSUtils;
import de.borekking.banSystem.util.discord.MyEmbedBuilder;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import java.util.stream.Collectors;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class PunishAutoCommand extends BSStandAloneCommand {

    private final Platform[] platforms;

    private final GeneralPunishmentHandler punishmentHandler;

    private final AutoReasonHandler autoReasonHandler;

    public PunishAutoCommand(String commandName, String commandDescription, String userOptionDescription, GeneralPunishmentHandler punishmentHandler, AutoReasonHandler autoReasonHandler, Platform... platforms) {
        super(commandName, commandDescription,
                new OptionData(OptionType.STRING, "user", userOptionDescription).setRequired(true),
                new OptionData(OptionType.STRING, "auto-id", "Auto id")
                        .addChoices(autoReasonHandler.getIds().stream().map(id -> {
                            AutoReason reason = autoReasonHandler.getReasonByID(id);
                            return new Command.Choice(reason.getName() + " (" + Duration.getMessage(reason.getDuration()) + ")", reason.getId());
                        }).collect(Collectors.toList())).setRequired(true)
        );

        this.punishmentHandler = punishmentHandler;
        this.autoReasonHandler = autoReasonHandler;
        this.platforms = platforms;
    }

    @Override
    public void perform(SlashCommandInteractionEvent event) {
        if (!BungeeMain.discordUserHasPermissions(event.getMember(), this.getPermission())) {
            BungeeMain.sendNoPermissionReply(event);
            return;
        }

        String user = event.getOption("user").getAsString();
        String autoID = event.getOption("auto-id").getAsString();

        long userID = this.getUserID(user, "");

        if (userID < 0) {
            event.replyEmbeds(new MyEmbedBuilder().color(Color.RED).description("User ID was not valid!").build()).queue();
            return;
        }

        net.dv8tion.jda.api.entities.User operatorDiscordUser = event.getUser();
        String operatorPlatformID = operatorDiscordUser.getId();
        long operatorID = BSUtils.getUserIDByDiscordIDAndCreateIfAbsent(operatorPlatformID, "");

        List<Punishment> punishments = this.perform(userID, operatorID, autoID);
        event.replyEmbeds(new MyEmbedBuilder().color(Color.GREEN).description("Created " + punishments.size() + " punishments and eventually deleted old ones.").build()).queue();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // Check Permission for MC users
        if (!BungeeMain.minecraftPlayerHasPermissions(sender, this.getPermission())) {
            return;
        }

        if (args.length < 2) {
            BungeeMain.sendMessage(sender, ChatColor.RED + "Too less arguments!", this.getUsage());
            return;
        }

        String user = args[0];
        long userID = this.getUserID(user, "");

        if (userID < 0) {
            BungeeMain.sendMessage(sender, ChatColor.RED + "User ID was not valid!");
            return;
        }

        long operatorID = -1L;
        if (sender instanceof ProxiedPlayer) {
            String operatorPlatformID = ((ProxiedPlayer) sender).getUniqueId().toString();
            operatorID = BSUtils.getUserIDByMinecraftIDAndCreateIfAbsent(operatorPlatformID, "");
        }

        List<Punishment> punishments = this.perform(userID, operatorID, args[1]);
        BungeeMain.sendMessage(sender, ChatColor.GREEN + "Created " + punishments.size() + " punishments and eventually deleted old ones.");
    }

    private List<Punishment> perform(long userID, long operatorID, String autoID) {
        List<Punishment> punishments = new ArrayList<>();
        AutoReason reason = this.getAutoReason(autoID);

        if (reason == null) return punishments;

        for (Platform platform : this.platforms) {
            Punishment punishment = reason.createPunishment(userID, operatorID, platform);

            this.punishmentHandler.punish(punishment);
            punishments.add(punishment);
        }

        return punishments;
    }

    private AutoReason getAutoReason(String idStr) {
        int idInt;

        try {
            idInt = Integer.parseInt(idStr);
        } catch (NumberFormatException exception) {
            return null;
        }

        return this.autoReasonHandler.getReasonByID(idInt);
    }

    private long getUserID(String platformID, String permissionOnAbsent) {
        for (Platform platform : this.platforms) {
            if (!platform.platformIDIsValid(platformID)) continue;

            return platform.getUserIDAncCreateIfAbsent(platformID, permissionOnAbsent);
        }
        return -1L;
    }
}
