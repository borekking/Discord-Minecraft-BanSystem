package de.borekking.banSystem.command.commands.other;

import de.borekking.banSystem.BungeeMain;
import de.borekking.banSystem.command.BSStandAloneCommand;
import de.borekking.banSystem.config.autoReason.AutoReason;
import de.borekking.banSystem.config.autoReason.AutoReasonHandler;
import de.borekking.banSystem.duration.Duration;
import de.borekking.banSystem.punishment.PunishmentType;
import de.borekking.banSystem.util.discord.MyEmbedBuilder;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import net.md_5.bungee.api.CommandSender;

public class AutoIDCommand extends BSStandAloneCommand {

    public AutoIDCommand() {
        super("auto-ids", "Shows auto-ids for given type",
                new OptionData(OptionType.STRING, "type", "The Type")
                        .setRequired(true)
                        .addChoices(new Command.Choice("Mute", PunishmentType.MUTE.name()),
                                new Command.Choice("Ban", PunishmentType.BAN.name())));
    }

    @Override
    public void perform(SlashCommandInteractionEvent event) {
        if (!BungeeMain.discordUserHasPermissions(event.getMember(), this.getPermission())) {
            BungeeMain.sendNoPermissionReply(event);
            return;
        }

        String type = event.getOption("type").getAsString();

        MyEmbedBuilder builder = new MyEmbedBuilder();
        AutoReasonHandler autoReasonHandler;

        if (PunishmentType.MUTE.name().equals(type)) {
            autoReasonHandler = BungeeMain.getInstance().getAutoMutes();
        } else if (PunishmentType.BAN.name().equals(type)) {
            autoReasonHandler = BungeeMain.getInstance().getAutoBans();
        } else {
            event.replyEmbeds(new MyEmbedBuilder().color(Color.RED).title("Error").description("Could not find type \"" + type + "\"!").build()).queue();
            return;
        }

        builder.color(Color.GRAY)
                .title("Auto IDs")
                .description("Auto IDs for " + type)
                .field(autoReasonHandler.getIds().stream().collect(Collectors.toMap(
                        id -> {
                            AutoReason reason = autoReasonHandler.getReasonByID(id);
                            return reason.getName() + " (" + id + ")";
                        },
                        id -> {
                            AutoReason reason = autoReasonHandler.getReasonByID(id);
                            return Duration.getMessage(reason.getDuration());
                        })
                ), false);

        event.replyEmbeds(builder.build()).queue();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // Check Permission for MC users
        if (!BungeeMain.minecraftPlayerHasPermissions(sender, this.getPermission())) {
            return;
        }

        if (args.length < 1) {
            BungeeMain.sendMessage(sender, "Illegal Amount of argumenten!", this.getUsage());
            return;
        }

        String type = args[0];

        AutoReasonHandler autoReasonHandler;

        if (PunishmentType.MUTE.name().equalsIgnoreCase(type)) {
            autoReasonHandler = BungeeMain.getInstance().getAutoMutes();
        } else if (PunishmentType.BAN.name().equalsIgnoreCase(type)) {
            autoReasonHandler = BungeeMain.getInstance().getAutoBans();
        } else {
            String[] availableTypes = Arrays.stream(PunishmentType.values()).map(punishment -> "   - " + punishment.name()).toArray(String[]::new);
            BungeeMain.sendMessage(sender, new String[] {"Could not find type \"" + type + "\"!", "Available Types: "}, availableTypes);
            return;
        }

        List<Integer> ids = autoReasonHandler.getIds();
        String[] arr = new String[ids.size() * 2];
        int i = 0;
        for (int id : ids) {
            AutoReason reason = autoReasonHandler.getReasonByID(id);
            arr[i++] = reason.getName() + " (" + id + ")";
            arr[i++] = "   " + Duration.getMessage(reason.getDuration());
        }

        BungeeMain.sendMessage(sender, new String[]{"Auto IDs for " + type + ":", ""}, arr);

    }
}
