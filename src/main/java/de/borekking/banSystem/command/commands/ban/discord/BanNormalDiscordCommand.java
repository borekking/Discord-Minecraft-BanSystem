package de.borekking.banSystem.command.commands.ban.discord;

import de.borekking.banSystem.command.BSStandAloneCommand;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.md_5.bungee.api.CommandSender;

public class BanNormalDiscordCommand extends BSStandAloneCommand {

    public BanNormalDiscordCommand() {
        super("name", "description");
    }

    @Override
    public void perform(SlashCommandInteractionEvent event) {

    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {

    }
}
