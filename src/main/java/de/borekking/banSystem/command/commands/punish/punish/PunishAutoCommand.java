package de.borekking.banSystem.command.commands.punish.punish;

import de.borekking.banSystem.BungeeMain;
import de.borekking.banSystem.command.BSStandAloneCommand;
import de.borekking.banSystem.config.autoReason.AutoReason;
import de.borekking.banSystem.config.autoReason.AutoReasonHandler;
import de.borekking.banSystem.punishment.GeneralPunishmentHandler;
import de.borekking.banSystem.punishment.Platform;
import de.borekking.banSystem.punishment.Punishment;

import de.borekking.banSystem.util.discord.MyEmbedBuilder;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;

public class PunishAutoCommand extends BSStandAloneCommand {

    private final Platform[] platforms;

    private final GeneralPunishmentHandler punishmentHandler;

    private final AutoReasonHandler autoReasonHandler;

    public PunishAutoCommand(String commandName, String commandDescription, String userOptionDescription, GeneralPunishmentHandler punishmentHandler, AutoReasonHandler autoReasonHandler, Platform... platforms) {
        super(commandName, commandDescription,
                new OptionData(OptionType.STRING, "user", userOptionDescription).setRequired(true),
                new OptionData(OptionType.STRING, "auto-id", "Auto id").setRequired(true));

        this.punishmentHandler = punishmentHandler;
        this.autoReasonHandler = autoReasonHandler;
        this.platforms = platforms;
    }

    @Override
    public void perform(SlashCommandInteractionEvent event) {
        String user = event.getOption("user").getAsString();
        String autoID = event.getOption("auto-id").getAsString();

        long userID = this.getUserID(user, "");

        if (userID < 0) {
            event.replyEmbeds(new MyEmbedBuilder().color(Color.RED).description("User ID was not valid!").build()).queue();
            return;
        }

        List<Punishment> punishments = this.perform(userID, autoID);
        event.replyEmbeds(new MyEmbedBuilder().color(Color.GREEN).description("Created " + punishments.size() + " punishments and eventually deleted old ones.").build()).queue();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
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

        List<Punishment> punishments = this.perform(userID, args[1]);
        BungeeMain.sendMessage(sender, ChatColor.GREEN + "Created " + punishments.size() + " punishments and eventually deleted old ones.");
    }

    private List<Punishment> perform(long userID, String autoID) {
        List<Punishment> punishments = new ArrayList<>();
        AutoReason reason = this.getAutoReason(autoID);

        if (reason == null) return punishments;

        for (Platform platform : this.platforms) {
            Punishment punishment = reason.createPunishment(userID, -1L, platform);

            this.punishmentHandler.punish(punishment);
            punishments.add(punishment);
        }

        return punishments;
    }

    private AutoReason getAutoReason(String idStr) {
        int idInt;

        try {
            idInt = Integer.parseInt(idStr);
        } catch(NumberFormatException exception) {
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
