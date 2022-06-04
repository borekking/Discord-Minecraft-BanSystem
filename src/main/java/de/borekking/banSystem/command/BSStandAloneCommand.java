package de.borekking.banSystem.command;

import de.borekking.banSystem.util.JavaUtils;
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
        this.options.addAll(Arrays.stream(optionData).filter(obj -> !Objects.isNull(obj)).collect(Collectors.toList())); // Make sure only non-null objects are added
    }

    @Override
    public String[] getUsage() {
        String[] arr1 = super.getUsage();
        String[] arr2 = this.options.stream().map(option -> String.format("   %s: %s (%s)", option.getName(), option.getType(), option.getDescription())).toArray(String[]::new);
        return JavaUtils.mergeArrays(arr1, arr2);
    }

    public SlashCommandData getCommandData() {
        return super.getCommandData().addOptions(this.options);
    }

    public List<OptionData> getOptions() {
        return new ArrayList<>(this.options); // Copy of list
    }
}