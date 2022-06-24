package de.borekking.banSystem.command.commands.user;

import de.borekking.banSystem.BungeeMain;
import de.borekking.banSystem.command.BSStandAloneCommand;
import de.borekking.banSystem.punishment.Platform;
import de.borekking.banSystem.punishment.user.User;
import de.borekking.banSystem.punishment.user.UserManager;
import de.borekking.banSystem.util.discord.MyEmbedBuilder;

import java.awt.Color;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import net.md_5.bungee.api.CommandSender;

public class UserMergeCommand extends BSStandAloneCommand {

    // /merge <m/d> <userA> <m/d> <userB>

    public UserMergeCommand() {
        super("merge", "Merge two users",
                new OptionData(OptionType.STRING, "platform-a", "User A's platform").setRequired(true)
                        .addChoices(new Command.Choice("Discord", Platform.DISCORD.name()), new Command.Choice("Minecraft", Platform.MINECRAFT.name())),
                new OptionData(OptionType.STRING, "user-a", "User A's Platform ID corresponding to platformA").setRequired(true),
                new OptionData(OptionType.STRING, "platform-b", "User B's platform").setRequired(true)
                        .addChoices(new Command.Choice("Discord", Platform.DISCORD.name()), new Command.Choice("Minecraft", Platform.MINECRAFT.name())),
                new OptionData(OptionType.STRING, "user-b", "User B's Platform ID corresponding to platformB").setRequired(true));
    }

    @Override
    public void perform(SlashCommandInteractionEvent event) {
        String platformStrA = event.getOption("platform-a").getAsString();
        String userIDA = event.getOption("user-a").getAsString();
        String platformStrB = event.getOption("platform-b").getAsString();
        String userIDB = event.getOption("user-b").getAsString();

        User userA = this.getUser(platformStrA, userIDA);
        User userB = this.getUser(platformStrB, userIDB);

        if (userA == null || userB == null) {
            event.replyEmbeds(new MyEmbedBuilder()
                    .color(Color.RED)
                    .title("Error")
                    .description("Illegal Arguments! Please check your arguments.")
                    .build()).queue();
            return;
        }

        // Compare by ID
        if (userA.equals(userB)) {
            event.replyEmbeds(new MyEmbedBuilder()
                    .color(Color.RED)
                    .title("Error")
                    .description("Users \"" + userA.getName() + "\" and \"" + userB.getName() + "\" are already merged!")
                    .build()).queue();
            return;
        }

        UserManager userManager = BungeeMain.getInstance().getUserManager();
        userManager.merge(userA.getId(), userB.getId());

        event.replyEmbeds(new MyEmbedBuilder()
                .color(Color.GREEN)
                .title("Success")
                .description("Merged user \"" + userA.getName() + "\" and user \"" + userB.getName() + "\".")
                .build()).queue();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // Check Permission for MC users
        if (!BungeeMain.minecraftPlayerHasPermissions(sender)) {
            return;
        }

        if (args.length < 4) {
            BungeeMain.sendMessage(sender, "Too less arguments!", this.getUsage());
            return;
        }

        String platformStrA = args[0];
        String userIDA = args[1];
        String platformStrB = args[2];
        String userIDB = args[3];

        User userA = this.getUser(platformStrA, userIDA);
        User userB = this.getUser(platformStrB, userIDB);

        if (userA == null || userB == null) {
            BungeeMain.sendMessage(sender, "Error:", "Illegal Arguments! Please check your arguments.");
            return;
        }

        // Compare by ID
        if (userA.equals(userB)) {
            BungeeMain.sendMessage(sender, "Error: ", "Users \"" + userA.getName() + "\" and \"" + userB.getName() + "\" are already merged!");
            return;
        }

        UserManager userManager = BungeeMain.getInstance().getUserManager();
        userManager.merge(userA.getId(), userB.getId());

        BungeeMain.sendMessage(sender, "Success:", "Merged user \"" + userA.getName() + "\" and user \"" + userB.getName() + "\".");
    }

    private User getUser(String platformStr, String userIDStr) {
        Platform platform = Platform.getPlatform(platformStr);
        if (platform == null) return null;

        long userID = BungeeMain.getUserID(platform, userIDStr);
        return BungeeMain.getUser(userID);
    }
}
