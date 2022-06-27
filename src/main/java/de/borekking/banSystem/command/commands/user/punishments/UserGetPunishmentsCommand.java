package de.borekking.banSystem.command.commands.user.punishments;

import de.borekking.banSystem.BungeeMain;
import de.borekking.banSystem.command.BSStandAloneCommand;
import de.borekking.banSystem.duration.Duration;
import de.borekking.banSystem.punishment.GeneralPunishmentHandler;
import de.borekking.banSystem.punishment.Platform;
import de.borekking.banSystem.punishment.Punishment;
import de.borekking.banSystem.user.User;
import de.borekking.banSystem.util.discord.MyEmbedBuilder;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.md_5.bungee.api.CommandSender;

public class UserGetPunishmentsCommand extends BSStandAloneCommand {

    private final GeneralPunishmentHandler[] punishmentHandlers;

    public UserGetPunishmentsCommand(GeneralPunishmentHandler[] punishmentHandlers) {
        super("get-punishments", "Get all punishments of a user",
                new OptionData(OptionType.STRING, "platform", "Platform (discord/minecraft)")
                        .setRequired(true)
                        .addChoices(new Command.Choice("Discord", Platform.DISCORD.name()), new Command.Choice("Minecraft", Platform.MINECRAFT.name())),
                new OptionData(OptionType.STRING, "platform-id", "ID: Minecraft (uuid/name) or Discord (id/tag)")
                        .setRequired(true));

        this.punishmentHandlers = punishmentHandlers;
    }

    @Override
    public void perform(SlashCommandInteractionEvent event) {
        if (!BungeeMain.discordUserHasPermissions(event.getMember(), this.getPermission())) {
            BungeeMain.sendNoPermissionReply(event);
            return;
        }

        String platformStr = event.getOption("platform").getAsString();
        String platformIDStr = event.getOption("platform-id").getAsString();

        Platform platform = Platform.getPlatform(platformStr);

        if (platform == null) {
            event.replyEmbeds(new MyEmbedBuilder()
                    .color(Color.RED)
                    .title("Error")
                    .description("Could not find platform \"" + platformStr + "\"!")
                    .build()).queue();
            return;
        }

        long userID = BungeeMain.getUserID(platform, platformIDStr);
        User user = BungeeMain.getUser(userID);

        if (user == null) {
            event.replyEmbeds(new MyEmbedBuilder()
                    .color(Color.RED)
                    .title("Error")
                    .description("Could not find user!").build()).queue();
            return;
        }

        List<MessageEmbed> embeds = new ArrayList<>();

        for (GeneralPunishmentHandler punishmentHandler : this.punishmentHandlers) {
            List<MessageEmbed> currentEmbedList = punishmentHandler.getOldPunishments(userID).stream().filter(Objects::nonNull)
                    .map(punishment -> this.createDCMessage(punishmentHandler, punishment))
                    .collect(Collectors.toList());

            for (Platform platform1 : Platform.values()) {
                Punishment punishment = punishmentHandler.getPunishment(userID, platform1);
                if (punishment == null) continue;
                currentEmbedList.add(this.createDCMessage(punishmentHandler, punishment));
            }

            embeds.addAll(currentEmbedList);
        }

        if (embeds.isEmpty()) {
            event.replyEmbeds(new MyEmbedBuilder().color(Color.GRAY)
                    .title("Not Punishments!")
                    .description("Could not find any punishments matching the requested user.").build()).queue();
            return;
        }

        event.replyEmbeds(embeds).queue();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // Check Permission for MC users
        if (!BungeeMain.minecraftPlayerHasPermissions(sender, this.getPermission())) {
            return;
        }

        if (args.length < 2) {
            BungeeMain.sendMessage(sender, "Too less arguments!", this.getUsage());
            return;
        }

        String platformStr = args[0];
        String platformIDStr = args[1];

        Platform platform = Platform.getPlatform(platformStr);

        if (platform == null) {
            BungeeMain.sendMessage(sender, "Error:", "Could not find platform \"" + platformStr + "\"!");
            return;
        }

        long userID = BungeeMain.getUserID(platform, platformIDStr);
        User user = BungeeMain.getUser(userID);

        if (user == null) {
            BungeeMain.sendMessage(sender, "Error", "Could not find user!");
            return;
        }

        boolean foundAny = false;

        for (GeneralPunishmentHandler punishmentHandler : this.punishmentHandlers) {
            List<String[]> currentEmbedList = punishmentHandler.getOldPunishments(userID).stream().filter(Objects::nonNull)
                    .map(punishment -> this.createMCMessage(punishmentHandler, punishment)).collect(Collectors.toList());

            for (Platform platform1 : Platform.values()) {
                Punishment punishment = punishmentHandler.getPunishment(userID, platform1);
                if (punishment == null) continue;
                currentEmbedList.add(this.createMCMessage(punishmentHandler, punishment));
            }

            if (currentEmbedList.size() > 0) {
                foundAny = true;
                BungeeMain.sendMessage(sender, currentEmbedList.toArray(new String[0][0]));
            }
        }

        if (!foundAny) {
            BungeeMain.sendMessage(sender, "Could not find any punishments matching the requested user.");
        }
    }

    private MessageEmbed createDCMessage(GeneralPunishmentHandler punishmentHandler, Punishment punishment) {
        return new MyEmbedBuilder()
                .color(Color.GRAY)
                .title(punishmentHandler.getName())
                .field("Reason", punishment.getReason(), false)
                .field("Date", new Date(punishment.getTimestamp()).toString(), false)
                .field("Duration", Duration.getMessage(punishment.getDuration()), false)
                .field("Platform", punishment.getPlatform().name(), false)
//                            .field("Operator", punishment.getOperatorID(), false)
                .build();
    }

    private String[] createMCMessage(GeneralPunishmentHandler punishmentHandler, Punishment punishment) {
        return new String[]{
                punishmentHandler.getName(),
                "   Reason: " + punishment.getReason(),
                "   Date: " + new Date(punishment.getTimestamp()),
                "   Duration: " + Duration.getMessage(punishment.getDuration()),
                "   Platform: " + punishment.getPlatform().name(),
//                            .field("Operator", punishment.getOperatorID(), false)
                " "};
    }
}
