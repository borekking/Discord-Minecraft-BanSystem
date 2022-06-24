package de.borekking.banSystem.discord.listener;

import de.borekking.banSystem.BungeeMain;
import de.borekking.banSystem.command.BSCommand;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import org.jetbrains.annotations.NotNull;

public class SlashCommandListener extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String name = event.getName();

        // Get Command from CommandHandler from BungeeMain
        BSCommand command = BungeeMain.getInstance().getCommandHandler().getCommand(name);

        command.perform(event);
    }
}
