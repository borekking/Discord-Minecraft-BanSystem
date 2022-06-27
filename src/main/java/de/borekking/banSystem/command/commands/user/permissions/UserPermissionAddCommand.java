package de.borekking.banSystem.command.commands.user.permissions;

import de.borekking.banSystem.BungeeMain;
import de.borekking.banSystem.command.BSStandAloneCommand;
import de.borekking.banSystem.punishment.Platform;
import de.borekking.banSystem.user.UserManager;
import de.borekking.banSystem.util.discord.MyEmbedBuilder;

import java.awt.Color;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import net.md_5.bungee.api.CommandSender;

public class UserPermissionAddCommand extends BSStandAloneCommand {

    public UserPermissionAddCommand() {
        super("add", "Add a user's permissions",
                new OptionData(OptionType.STRING, "platform", "Platform (discord/minecraft)")
                        .setRequired(true)
                        .addChoices(new Command.Choice("Discord", Platform.DISCORD.name()), new Command.Choice("Minecraft", Platform.MINECRAFT.name())),
                new OptionData(OptionType.STRING, "platform-id", "ID: Minecraft (uuid/name) or Discord (id/tag)")
                        .setRequired(true),
                new OptionData(OptionType.STRING, "permissions", "Permissions added, connected by ; (eg. \"durations;bansystem\")")
                        .setRequired(true));
    }

    @Override
    public void perform(SlashCommandInteractionEvent event) {
        if (!BungeeMain.discordUserHasPermissions(event.getMember(), this.getPermission())) {
            BungeeMain.sendNoPermissionReply(event);
            return;
        }

        String platformStr = event.getOption("platform").getAsString();
        String platformIDStr = event.getOption("platform-id").getAsString();
        String permissions = event.getOption("permissions").getAsString();

        Platform platform = Platform.getPlatform(platformStr);

        if (platform == null) {
            event.replyEmbeds(new MyEmbedBuilder()
                    .color(Color.RED)
                    .title("Error")
                    .description("Could not find platform \"" + platformStr + "\"!")
                    .build()).queue();
            return;
        }

        long userID = BungeeMain.getUserID(platform, platformIDStr);

        UserManager userManager = BungeeMain.getInstance().getUserManager();
        String newPermissions = userManager.addPermissions(userID, permissions);

        if (newPermissions == null) {
            event.replyEmbeds(new MyEmbedBuilder()
                    .color(Color.RED)
                    .title("Error")
                    .description("Error occurred! Permissions Could not be added! Probably the user does not exist. Please add the user using /user add.")
                    .build()).queue();
            return;
        }

        event.replyEmbeds(new MyEmbedBuilder()
                .color(Color.GREEN)
                .title("Success")
                .description("Added permissions!")
                .field("New Permissions", "\"" + newPermissions + "\"", false)
                .build()).queue();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // Check Permission for MC users
        if (!BungeeMain.minecraftPlayerHasPermissions(sender, this.getPermission())) {
            return;
        }

        if (args.length < 3) {
            BungeeMain.sendMessage(sender, "Too less arguments!", this.getUsage());
            return;
        }

        String platformStr = args[0];
        String platformIDStr = args[1];
        String permissions = args[2];

        Platform platform = Platform.getPlatform(platformStr);

        if (platform == null) {
            BungeeMain.sendMessage(sender, "Error:", "Could not find platform \"" + platformStr + "\"!");
            return;
        }

        long userID = BungeeMain.getUserID(platform, platformIDStr);

        UserManager userManager = BungeeMain.getInstance().getUserManager();
        String newPermissions = userManager.addPermissions(userID, permissions);

        if (newPermissions == null) {
            BungeeMain.sendMessage(sender, "Error occurred!",
                    "Permissions Could not be added! Probably the user does not exist.",
                    "Please add the user using /user add.");
            return;
        }

        BungeeMain.sendMessage(sender, "Success!", "Added permissions!", "New Permissions:", newPermissions);
    }
}
