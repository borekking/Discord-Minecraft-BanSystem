package de.borekking.banSystem.command.commands.user;

import de.borekking.banSystem.BungeeMain;
import de.borekking.banSystem.command.BSStandAloneCommand;
import de.borekking.banSystem.punishment.Platform;
import de.borekking.banSystem.user.User;
import de.borekking.banSystem.util.discord.MyEmbedBuilder;

import java.awt.Color;
import java.util.UUID;
import java.util.stream.Collectors;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import net.md_5.bungee.api.CommandSender;

public class UserGetCommand extends BSStandAloneCommand {

    public UserGetCommand() {
        super("get", "Get a user",
                new OptionData(OptionType.STRING, "platform", "Platform (discord/minecraft)")
                        .setRequired(true)
                        .addChoices(new Command.Choice("Discord", Platform.DISCORD.name()), new Command.Choice("Minecraft", Platform.MINECRAFT.name())),
                new OptionData(OptionType.STRING, "platform-id", "ID: Minecraft (uuid/name) or Discord (id/tag)")
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
        User user = BungeeMain.getUser(userID);

        if (user == null) {
            event.replyEmbeds(new MyEmbedBuilder()
                    .color(Color.RED)
                    .title("Error")
                    .description("Could not find user!").build()).queue();
            return;
        }

        event.replyEmbeds(new MyEmbedBuilder()
                .color(Color.GRAY)
                .title(user.getName())
                .field("Name", user.getName(), false)
                .field("Permissions", "`" +  user.getPermissions() + "`", false)
                .field("UUIDs", user.getUuids().stream().map(UUID::toString).collect(Collectors.joining(", ")), false)
                .field("Discord IDs", user.getDiscordIDs().stream().map(String::valueOf).collect(Collectors.joining(", ")), false)
                .build()).queue();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // Check Permission for MC users
        if (!BungeeMain.minecraftPlayerHasPermissions(sender, this.getPermission())) {
            return;
        }

        if (args.length < 2) {
            BungeeMain.sendMessage(sender, "Too less arguments!", this.getUsage());
            return;
        }

        String platformStr = args[0];
        String platformIDStr = args[1];

        Platform platform = Platform.getPlatform(platformStr);

        if (platform == null) {
            BungeeMain.sendMessage(sender, "Error:", "Could not find platform \"" + platformStr + "\"!");
            return;
        }

        long userID = BungeeMain.getUserID(platform, platformIDStr);
        User user = BungeeMain.getUser(userID);

        if (user == null) {
            BungeeMain.sendMessage(sender, "Error", "Could not find user!");
            return;
        }

        BungeeMain.sendMessage(sender,
                "Name:", "   " + user.getName(),
                        "Permissions:", "   " + user.getPermissions(),
                        "UUIDs", "   " + user.getUuids().stream().map(UUID::toString).collect(Collectors.joining(", ")),
                        "Discord IDs", "   " + user.getDiscordIDs().stream().map(String::valueOf).collect(Collectors.joining(", ")));
    }
}
