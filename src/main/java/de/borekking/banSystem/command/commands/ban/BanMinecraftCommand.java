package de.borekking.banSystem.command.commands.ban;

import de.borekking.banSystem.BungeeMain;
import de.borekking.banSystem.command.BSStandAloneCommand;
import de.borekking.banSystem.duration.Duration;
import de.borekking.banSystem.punishment.GeneralPunishmentHandler;
import de.borekking.banSystem.punishment.Platform;
import de.borekking.banSystem.punishment.Punishment;
import de.borekking.banSystem.punishment.user.UserManager;
import de.borekking.banSystem.util.JavaUtils;
import de.borekking.banSystem.util.discord.MyEmbedBuilder;
import de.borekking.banSystem.util.minecraft.MinecraftUUIDUtils;

import java.awt.Color;
import java.util.UUID;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import net.md_5.bungee.api.CommandSender;

public class BanMinecraftCommand extends BSStandAloneCommand {

    /*
     * Command to ban a minecraft user.
     *
     */

    public BanMinecraftCommand() {
        super("minecraft", "Ban a minecraft user",
                new OptionData(OptionType.STRING, "user", "Minecraft username or UUID").setRequired(true),
                new OptionData(OptionType.STRING, "duration", "Duration of ban").setRequired(true),
                new OptionData(OptionType.STRING, "reason", "Optional: Provide Reason"));
    }

    @Override
    public void perform(SlashCommandInteractionEvent event) {
        String userStr = event.getOption("user").getAsString();
        String durationStr = event.getOption("duration").getAsString();

        OptionMapping reasonOption = event.getOption("reason");
        String reason = reasonOption != null ? reasonOption.getAsString() : "";

        if (this.ban(userStr, durationStr, reason)) {
            event.replyEmbeds(new MyEmbedBuilder().color(Color.GREEN).title("Success").description("Banned user " + userStr + " for reason " + reason).build()).queue();
        } else {
            event.replyEmbeds(new MyEmbedBuilder().color(Color.RED).title("Error").description("Could not ban " + userStr + "!").build()).queue();
        }
    }

    @Override
    public void execute(CommandSender commandSender, String[] args) {
        if (args.length < 2) {
            BungeeMain.sendMessage(commandSender, "Too lees arguments!", this.getUsage());
            return;
        }

        String userStr = args[0];
        String duration = args[1];
        String reason = args.length > 2 ? JavaUtils.getArrayAsString(args, 2, args.length) : "";

        if (this.ban(userStr, duration, reason)) {
            BungeeMain.sendMessage(commandSender, "Banned user " + userStr + " for " + reason + "(" + duration + ").");
        } else {
            BungeeMain.sendMessage(commandSender, "Error: could not ban user " + userStr + "!");
        }
    }

    // Return if ban was successful
    // TODO be more precise in return value?
    private boolean ban(String playerIdentifier, String duration, String reason) {
        // Get uuid from playerIdentifier
        UUID uuid = MinecraftUUIDUtils.getUUID(playerIdentifier);
        if (uuid == null) {
            try {
                uuid = MinecraftUUIDUtils.getUUIDFromName(playerIdentifier);
            } catch (MinecraftUUIDUtils.NoSuchPlayerException ignored) {
                return false; // TODO Error?
            }
        }
        if (uuid == null) return false;
        String uuidStr = uuid.toString();

        // Get duration as long
        long durationLong;
        try {
            durationLong = Duration.getValueOfOne(duration);
        } catch (Duration.IllegalDurationException e) {
            return false; // TODO Error?
        }

        // Make sure reason is not null
        reason = reason == null ? "" : reason;

        // Get userID from uuid
        long userID = BungeeMain.getUserID(Platform.MINECRAFT, uuidStr);

        // If user does not exist, create user
        if (userID < 0) {
            UserManager userManager = BungeeMain.getInstance().getUserManager();
            userID = userManager.addUser(Platform.MINECRAFT, uuidStr, "");
        }

        // Create Punishment
        long currentTimestamp = System.currentTimeMillis();
        // TODO Operator ID
        Punishment punishment = new Punishment(userID, -1L, currentTimestamp, currentTimestamp + durationLong, Platform.MINECRAFT, reason);

        // Actual ban
        GeneralPunishmentHandler banHandler = BungeeMain.getInstance().getBanHandler();
        banHandler.punish(punishment, Platform.MINECRAFT);

        return true;
    }
}
