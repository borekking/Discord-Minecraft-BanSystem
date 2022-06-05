package de.borekking.banSystem.command.commands.ban.minecraft;

import de.borekking.banSystem.command.BSStandAloneCommand;
import de.borekking.banSystem.config.autoReason.AutoReasonHandler;

import java.util.stream.Collectors;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import net.md_5.bungee.api.CommandSender;

public class BanMinecraftAuto extends BSStandAloneCommand {

    public BanMinecraftAuto(AutoReasonHandler handler) {
        super("auto", "Ban a minecraft user (auto)",
                new OptionData(OptionType.STRING, "user", "Minecraft username or UUID").setRequired(true),
                new OptionData(OptionType.INTEGER, "id", "Duration of ban").addChoices(handler.getIds().stream().map(id -> new Command.Choice(handler.getReasonByID(id).getName(), id)).collect(Collectors.toList())).setRequired(true)
        );
    }

    @Override
    public void perform(SlashCommandInteractionEvent event) {

    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {

    }
}
