package de.borekking.banSystem.command.commands.other;

import de.borekking.banSystem.BungeeMain;
import de.borekking.banSystem.command.BSStandAloneCommand;
import de.borekking.banSystem.duration.TimeEnum;
import de.borekking.banSystem.util.discord.MyEmbedBuilder;

import java.awt.Color;
import java.util.List;
import java.util.Map;

import java.util.stream.Collectors;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import net.md_5.bungee.api.CommandSender;

public class DurationsCommand extends BSStandAloneCommand {

    public DurationsCommand() {
        super("durations", "Shows all durations");
    }

    @Override
    public void perform(SlashCommandInteractionEvent event) {
        Map<String, String> fields = TimeEnum.getDecreasingValues().stream()
                .collect(Collectors.toMap(TimeEnum::getName, TimeEnum::getShortName));

        event.replyEmbeds(new MyEmbedBuilder()
                .color(Color.GRAY)
                .title("Available Durations")
                .field(fields, true)
                .build()).queue();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // Check Permission for MC users
        if (!BungeeMain.minecraftPlayerHasPermissions(sender)) {
            return;
        }

        BungeeMain.sendMessage(sender, "Available Durations", this.createCommandFieldsMC());
    }

    private String[] createCommandFieldsMC() {
        List<TimeEnum> durations = TimeEnum.getDecreasingValues();
        String[] arr = new String[durations.size()];

        int i = 0;
        for (TimeEnum duration : durations) {
            arr[i++] = duration.getName() + ": " + duration.getShortName();
        }

        return arr;
    }
}
