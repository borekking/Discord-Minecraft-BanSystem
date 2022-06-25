package de.borekking.banSystem.command;

import de.borekking.banSystem.permission.PermissionUtil;
import java.util.HashMap;
import java.util.Map;

public class CommandBuilder {

    // Class to Build BSBaseCommands (meaning Commands with subCommands, subCommandGroups)
    // StandAloneCommands obviously don't need a builder.

    private final BSBaseCommand command;

    private final Map<String, BSSubCommandGroup> subCommandGroups;

    public CommandBuilder(String name, String description) {
        this.command = new BSBaseCommand(name, description);

        this.subCommandGroups = new HashMap<>();
    }

    // Add a single SubCommand
    public CommandBuilder addSubCommand(BSStandAloneCommand subCommand) {
        subCommand.setPermission(PermissionUtil.mergeCommandPermissions(this.command.getName(), subCommand.getPermission()));
        this.command.addSubCommands(subCommand);
        return this;
    }

    public CommandBuilder addSubCommandGroup(String name, String description) {
        this.subCommandGroups.put(name, new BSSubCommandGroup(name, description));
        return this;
    }

    // Add a subCommand in a subCommandGroup
    public CommandBuilder addSubCommand(String subCommandGroup, BSStandAloneCommand subCommand) {
        // Check if subCommandGroup already exists
        BSSubCommandGroup group = this.subCommandGroups.get(subCommandGroup);
        if (group == null) return this; // Group does not exist!

        subCommand.setPermission(PermissionUtil.mergeCommandPermissions(this.command.getName(), subCommandGroup, subCommand.getPermission()));
        group.addSubCommands(subCommand);
        return this;
    }

    public BSCommand create() {
        // Add subCommandGroups
        for (BSSubCommandGroup group : this.subCommandGroups.values()) {
            this.command.addSubCommandGroups(group);
        }

        return this.command;
    }
}
