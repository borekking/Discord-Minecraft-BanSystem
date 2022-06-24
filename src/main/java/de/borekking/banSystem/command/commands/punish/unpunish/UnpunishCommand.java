package de.borekking.banSystem.command.commands.punish.unpunish;

import de.borekking.banSystem.BungeeMain;
import de.borekking.banSystem.command.BSStandAloneCommand;
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

public class UnpunishCommand extends BSStandAloneCommand {

    private final Platform[] platforms;

    private final GeneralPunishmentHandler punishmentHandler;

    public UnpunishCommand(String commandName, String commandDescription, String userOptionDescription, GeneralPunishmentHandler punishmentHandler, Platform... platforms) {
        super(commandName, commandDescription, new OptionData(OptionType.STRING, "user", userOptionDescription).setRequired(true));

        this.platforms = platforms;
        this.punishmentHandler = punishmentHandler;
    }

    @Override
    public void perform(SlashCommandInteractionEvent event) {
        String user = event.getOption("user").getAsString();

        net.dv8tion.jda.api.entities.User operatorDiscordUser = event.getUser();
        String operatorPlatformID = operatorDiscordUser.getId();
        long operatorID = BSUtils.getUserIDByDiscordIDAndCreateIfAbsent(operatorPlatformID, "");

        List<Punishment> punishments = this.unPunish(operatorID, user);

        if (punishments.size() == 0) {
            event.replyEmbeds(new MyEmbedBuilder().color(Color.RED).title("Error").description("Could not find any matching user!").build()).queue();
            return;
        }

        event.replyEmbeds(new MyEmbedBuilder().color(Color.GREEN).title("Success").description("Removed " + punishments.size() + " punishments.").build()).queue();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            BungeeMain.sendMessage(sender, ChatColor.RED + "Too less arguments!", this.getUsage());
            return;
        }

        long operatorID = -1L;
        if (sender instanceof ProxiedPlayer) {
            String operatorPlatformID = ((ProxiedPlayer) sender).getUniqueId().toString();
            operatorID = BSUtils.getUserIDByMinecraftIDAndCreateIfAbsent(operatorPlatformID, "");
        }

        String user = args[0];
        List<Punishment> punishments = this.unPunish(operatorID, user);

        if (punishments.size() == 0) {
            BungeeMain.sendMessage(sender, ChatColor.RED + "Could not find any matching user!");
            return;
        }

        BungeeMain.sendMessage(sender, ChatColor.GREEN + "Removed " + punishments.size() + " punishments.");
    }

    private long getUserID(String userStr) {
        for (Platform platform : this.platforms) {
            long id = platform.getUserIDAncCreateIfAbsent(userStr, "");
            if (id >= 0) return id;
        }

        return -1L;
    }

    private List<Punishment> unPunish(long operatorID, String userStr) {
        List<Punishment> punishments = new ArrayList<>();
        long userID = this.getUserID(userStr);

        if (userID < 0) return punishments;

        for (Platform platform : this.platforms) {
            Punishment punishment = punishmentHandler.getPunishment(userID, platform);
            if (punishment == null) continue;

            punishment.setOperatorID(operatorID);

            this.punishmentHandler.unPunish(punishment);
            punishments.add(punishment);
        }

        return punishments;
    }
}
