package de.borekking.banSystem.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public abstract class BSStandAloneCommand extends BSCommand {

    // Command without subcommands

    private final List<OptionData> options;

    public BSStandAloneCommand(String name, String description, OptionData... optionData) {
        super(name, description);

        this.options = new ArrayList<>();
        this.options.addAll(Arrays.stream(optionData).filter(Objects::isNull).collect(Collectors.toList())); // Make sure only non-null objects are added
    }

    public SlashCommandData getCommandData() {
        return super.getCommandData().addOptions(this.options);
    }

    public List<OptionData> getOptions() {
        return new ArrayList<>(this.options); // Copy of list
    }
}