package de.borekking.banSystem.command.commands.user;

import de.borekking.banSystem.BungeeMain;
import de.borekking.banSystem.command.BSStandAloneCommand;
import de.borekking.banSystem.punishment.Platform;
import de.borekking.banSystem.user.User;
import de.borekking.banSystem.util.discord.MyEmbedBuilder;

import java.awt.Color;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import net.md_5.bungee.api.CommandSender;

public class UserAddCommand extends BSStandAloneCommand {

    // /user add <platform> <platformID>

    public UserAddCommand() {
        super("add", "Add an user", "user.add",
                new OptionData(OptionType.STRING, "platform", "Platform (discord/minecraft)")
                        .setRequired(true)
                        .addChoices(new Command.Choice("Discord", Platform.DISCORD.name()), new Command.Choice("Minecraft", Platform.MINECRAFT.name())),
                new OptionData(OptionType.STRING, "platform-id", "ID: Minecraft (uuid/name) or Discord (id/tag)")
                        .setRequired(true));
    }

    @Override
    public void perform(SlashCommandInteractionEvent event) {
        if (!BungeeMain.discordUserHasPermissions(event.getUser(), this.getPermission())) {
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

        if (user != null) {
            event.replyEmbeds(new MyEmbedBuilder()
                    .color(Color.RED)
                    .title("Error")
                    .description("User \"" + platformIDStr + "\" already existed!")
                    .build()).queue();
            return;
        }

        long newUserID = BungeeMain.getInstance().getUserManager().addUser(platform, platformIDStr, "");

        if (newUserID < 0L) {
            event.replyEmbeds(new MyEmbedBuilder()
                    .color(Color.RED)
                    .title("Error")
                    .description("Given userID was incorrect!")
                    .build()).queue();
                return;
        }

        User newUser = BungeeMain.getUser(newUserID);

        event.replyEmbeds(new MyEmbedBuilder()
                .color(Color.GREEN)
                .title("Success")
                .description("Created new user \"" + newUser.getName() + "\".")
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

        if (user != null) {
            BungeeMain.sendMessage(sender, "Error:", "User \"" + platformIDStr + "\" already existed!");
            return;
        }

        long newUserID = BungeeMain.getInstance().getUserManager().addUser(platform, platformIDStr, "");

        if (newUserID < 0L) {
            BungeeMain.sendMessage(sender, "Error:", "Given userID was incorrect!");
            return;
        }

        User newUser = BungeeMain.getUser(newUserID);

        BungeeMain.sendMessage(sender, "Success:", "Created new user \"" + newUser.getName() + "\".");
    }
}
