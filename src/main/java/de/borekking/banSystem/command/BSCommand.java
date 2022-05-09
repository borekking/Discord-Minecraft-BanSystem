package de.borekking.banSystem.command;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

import net.md_5.bungee.api.plugin.Command;

public abstract class BSCommand extends Command implements CommandPart {

    private final String name, description;

    BSCommand(String name, String description) {
        super(name);

        this.name = name;
        this.description = description;
    }

    // Discord: Handle SlashCommands
    public abstract void perform(SlashCommandInteractionEvent event);

    // Discord: SlashCommandData / CommandData (old)
    public SlashCommandData getCommandData() {
        return new CommandDataImpl(this.name, this.description);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }
}