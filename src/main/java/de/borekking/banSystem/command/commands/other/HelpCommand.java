package de.borekking.banSystem.command.commands.other;

import de.borekking.banSystem.BungeeMain;
import de.borekking.banSystem.command.BSCommand;
import de.borekking.banSystem.command.BSStandAloneCommand;
import de.borekking.banSystem.util.discord.MyEmbedBuilder;

import java.awt.Color;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import net.md_5.bungee.api.CommandSender;

public class HelpCommand extends BSStandAloneCommand {

    // Help Command named "bansystem"

    private final BSCommand[] commands;

    public HelpCommand(BSCommand[] commands) {
        super("bansystem", "Provides help");

        this.commands = commands;
    }

    @Override
    public void perform(SlashCommandInteractionEvent event) {
        event.replyEmbeds(new MyEmbedBuilder().title("Help").color(Color.GRAY)
                .description("Description for all commands you have permissions for.") // TODO Permission stuff
                .field(this.createCommandFields(), false)
                .build()).queue();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // Check Permission for MC users
        if (!BungeeMain.minecraftPlayerHasPermissions(sender)) {
            return;
        }

        // TODO Permission stuff
        BungeeMain.sendMessage(sender, new String[] {"Description for all commands you have permissions for:", ""}, this.createCommandFieldsMC()); // TODO
    }

    private Map<String, String> createCommandFields() {
        return Arrays.stream(this.commands).collect(Collectors.toMap(BSCommand::getName, BSCommand::getDescription));
    }

    private String[] createCommandFieldsMC() {
        String[] arr = new String[this.commands.length * 2];

        int i = 0;
        for (BSCommand command : this.commands) {
            arr[i++] = command.getName() + ":";
            arr[i++] = "   " + command.getDescription();
        }

        return arr;
    }
}
