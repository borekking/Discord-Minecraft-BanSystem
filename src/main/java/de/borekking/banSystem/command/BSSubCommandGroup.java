package de.borekking.banSystem.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;

public class BSSubCommandGroup implements CommandPart {

    private final String name, description;

    private final Map<String, BSStandAloneCommand> subCommands;

    // Package Private because instances should only be created in builder (contained in this package)
    BSSubCommandGroup(String name, String description) {
        this.name = name;
        this.description = description;

        this.subCommands = new HashMap<>();
    }

    // Package Private because new subCommands should only be added in builder (contained in this package)
    void addSubCommands(BSStandAloneCommand... subCommands) {
        for (BSStandAloneCommand command : subCommands) {
            if (command == null) continue; // Make sure only non - null objects
            this.subCommands.put(command.getName(), command);
        }
    }

    public SubcommandGroupData getSubcommandGroupData() {
        // Return new SubcommandGroupData object containing name, description and subCommands.
        // SubCommands are obtained from subCommands Map-values, converted to SubcommandData (name, description).
        return new SubcommandGroupData(this.name, this.description).addSubcommands(this.subCommands.values()
                .stream().map(cmd -> new SubcommandData(cmd.getName(), cmd.getDescription())).collect(Collectors.toList()));
    }

    // Return subcommand by name
    public BSStandAloneCommand getSubCommand(String name) {
        return this.subCommands.get(name);
    }

    public Collection<BSStandAloneCommand> getSubCommands() {
        return new ArrayList<>(this.subCommands.values()); // Copy of subCommands
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