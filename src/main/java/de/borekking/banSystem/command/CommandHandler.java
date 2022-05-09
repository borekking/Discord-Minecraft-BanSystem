package de.borekking.banSystem.command;

import java.util.HashMap;
import java.util.Map;

public class CommandHandler {

    // Class for holding Commands and receiving them by name

    private final Map<String, BSCommand> commands;

    public CommandHandler(BSCommand... commands) {
        this.commands = new HashMap<>();

        for (BSCommand command : commands) {
            this.addCommand(command);
        }
    }

    // Get BSCommand by Name
    public BSCommand getCommand(String name) {
        return this.commands.get(name);
    }

    private void addCommand(BSCommand command) {
        this.commands.put(command.getName(), command);
    }
}
