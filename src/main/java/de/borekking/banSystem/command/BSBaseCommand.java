package de.borekking.banSystem.command;

import de.borekking.banSystem.BungeeMain;

import de.borekking.banSystem.punishment.Platform;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class BSBaseCommand extends BSCommand {

    // Command with subcommands

    /*
     * Command
     *    -> options
     *    -> SubCommand
     *           -> options
     *    -> subCommandGroup
     *        -> subCommand
     *               -> options
     *
     * (Discord) Note:
     *    - Command itself is not usable if there are subCommandGroups or subCommands
     *    - SubCommandGroups can not be used as commands (ig)
     *
     * see:
     *    https://discord.com/developers/docs/interactions/application-commands#subcommands-and-subcommand-groups
     *
     */

    private final Map<String, BSSubCommandGroup> subCommandGroups;
    private final Map<String, BSStandAloneCommand> subCommands;

    // Package Private because instances should only be created in builder (contained in this package)
    BSBaseCommand(String name, String description) {
        super(name, description);

        this.subCommandGroups = new HashMap<>();
        this.subCommands = new HashMap<>();
    }

    // Package Private because new subCommands should only be added in builder (contained in this package)
    void addSubCommands(BSStandAloneCommand... subCommands) {
        for (BSStandAloneCommand command : subCommands) {
            if (command == null) continue; // Skip if null

            String name = command.getName().toLowerCase();

            // Make sure no subCommand and subCommandGroup have the same name.
            // Check for doubled subCommands too.
            if (this.subCommandGroups.containsKey(name) || this.subCommands.containsKey(name)) {
                throw new IllegalArgumentException("Tried to add SubCommand (" + name +  ") but SubCommand or SubCommandGroup already existed!");
            }

            this.subCommands.put(name, command);
        }
    }

    // Package Private because new SubCommandGroups should only be added in builder (contained in this package)
    void addSubCommandGroups(BSSubCommandGroup... subCommandGroups) {
        for (BSSubCommandGroup subCommandGroup : subCommandGroups) {
            if (subCommandGroup == null) continue; // Skip if null

            String name = subCommandGroup.getName().toLowerCase();

            // Make sure no subCommand and subCommandGroup have the same name.
            // Check for doubled subCommands too.
            if (this.subCommandGroups.containsKey(name) || this.subCommands.containsKey(name)) {
                throw new IllegalArgumentException("Tried to add SubCommandGroup (" +  subCommandGroup.getName() +  ") but SubCommand or SubCommandGroup already existed!");
            }

            this.subCommandGroups.put(name, subCommandGroup);
        }
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // Check Permission for MC users
        if (BungeeMain.minecraftPlayerHasPermissions(sender)) {
            return;
        }

        if (args.length < 1) {
            // Send help message, with commands that can be used
            BungeeMain.sendMessage(sender, "Unknown Sub-Command/Sub-Command-Group!", "Available Sub-Commands:",
                    String.join(", ", this.subCommands.keySet()), "Available Sub-Command-Groups: ", String.join(", ", this.subCommandGroups.keySet()));
            return;
        }

        int argsRemove = 1;
        BSStandAloneCommand command = this.getSubCommand(args);

        // If subCommand wasn't found, check for SubCommandGroup
        if (command == null) {
            BSSubCommandGroup group = this.getGroup(args[0]);

            // If group exists, try to get subCommand
            if (group != null) {
                command = args.length < 2 ? null : group.getSubCommand(args[1]);

                // If the subCommand did not exist, send help message for this.group command
                if (command == null) {
                    // Send help message, with commands that can be used in group
                    BungeeMain.sendMessage(sender, "Unknown Sub-Command!", "Available Sub-Commands for \"/" + this.getName() + " " + group.getName() + "\":",
                            group.getSubCommands().stream().map(BSCommand::getName).collect(Collectors.joining(", ")));
                    return;
                }

                // Here the group's subCommand was found.
                argsRemove = 2;
            }
        }

        if (command == null) {
            // Send help message, with commands that can be used
            BungeeMain.sendMessage(sender, "Unknown Sub-Command/Sub-Command-Group!", "Available Sub-Commands:",
                    String.join(", ", this.subCommands.keySet()), "Available Sub-Command-Groups: ", String.join(", ", this.subCommandGroups.keySet()));
            return;
        }

        String[] newArgs = Arrays.copyOfRange(args, argsRemove, args.length);
        command.execute(sender, newArgs);
    }

    @Override
    public void perform(SlashCommandInteractionEvent event) {
        String group = event.getSubcommandGroup();
        String subCommand = event.getSubcommandName();

        BSStandAloneCommand command = this.getSubCommand(subCommand);
        if (command == null) {
            command = this.getSubCommandFromGroup(group, subCommand);
        }

        if (command == null) {
            // Send help message, with commands that can be used
            // TODO s.o.
            return;
        }

        command.perform(event);
    }

    public SlashCommandData getCommandData() {
        SlashCommandData commandData = super.getCommandData();

        // Add subCommands
        commandData.addSubcommands(this.createSubCommandDataList(this.subCommands.values()));

        // Add subCommandGroups
        commandData.addSubcommandGroups(this.subCommandGroups.values().stream()
                .map(group -> new SubcommandGroupData(group.getName(), group.getDescription())
                        .addSubcommands(this.createSubCommandDataList(group.getSubCommands()))).collect(Collectors.toList()));

        return commandData;
    }

    // Get direct SubCommand from args
    private BSStandAloneCommand getSubCommand(String[] args) {
        if (args.length < 1) return null; // There can not be a sub command if no args are provided

        return this.getSubCommand(args[0]);
    }

    // Get SubCommand from SubCommandGroup from args
    private BSStandAloneCommand getSubCommandFromGroup(String[] args) {
        if (args.length < 2) return null; // There can not be a sub command group if only one arg is provided

        return this.getSubCommandFromGroup(args[0], args[1]);
    }

    private BSSubCommandGroup getGroup(String group) {
        if (group == null) return null;

        return this.subCommandGroups.get(group.toLowerCase());
    }

    // Get direct SubCommand
    private BSStandAloneCommand getSubCommand(String subCommand) {
        if (subCommand == null) return null;
        return this.subCommands.get(subCommand.toLowerCase());
    }

    // Get SubCommand from SubCommandGroup
    private BSStandAloneCommand getSubCommandFromGroup(String group, String subCommand) {
        if (group == null || subCommand == null) return null;

        BSSubCommandGroup subCommandGroup = this.subCommandGroups.get(group.toLowerCase());
        if (subCommandGroup == null) return null;

        return subCommandGroup.getSubCommand(subCommand.toLowerCase());
    }

    private Collection<SubcommandData> createSubCommandDataList(Collection<BSStandAloneCommand> subCommands) {
        return subCommands.stream().map(this::getSubCommandData).collect(Collectors.toList());
    }

    private SubcommandData getSubCommandData(BSStandAloneCommand command) {
        return new SubcommandData(command.getName(), command.getDescription()).addOptions(command.getOptions());
    }
}