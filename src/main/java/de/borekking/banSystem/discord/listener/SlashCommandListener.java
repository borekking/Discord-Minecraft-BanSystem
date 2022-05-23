package de.borekking.banSystem.discord.listener;

import de.borekking.banSystem.BungeeMain;
import de.borekking.banSystem.command.BSCommand;
import de.borekking.banSystem.util.discord.MyEmbedBuilder;

import java.awt.Color;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import org.jetbrains.annotations.NotNull;

public class SlashCommandListener extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String name = event.getName();

        BSCommand command = BungeeMain.getInstance().getCommandHandler().getCommand(name); // Get Command from CommandHandler from BungeeMain

//        TODO Add Permission check
//        if () {
//            this.sendNoPermissions(event);
//            return;
//        }

        command.perform(event);
    }

    private void sendNoPermissions(SlashCommandInteractionEvent event) {
        event.replyEmbeds(new MyEmbedBuilder().color(Color.RED).title("ERROR").description("You don't have the required permissions to do that!").build()).complete();
    }
}
