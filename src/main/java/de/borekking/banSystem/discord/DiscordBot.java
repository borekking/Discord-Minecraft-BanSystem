package de.borekking.banSystem.discord;

import de.borekking.banSystem.BungeeMain;
import de.borekking.banSystem.command.BSCommand;
import de.borekking.banSystem.discord.listener.SlashCommandListener;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

public class DiscordBot {

    private JDA jda;
    private final Guild guild;

    public DiscordBot(String token, String guildId, BSCommand[] commands) {
        JDABuilder builder = JDABuilder.createLight(token);

        builder.setStatus(OnlineStatus.ONLINE);
        builder.setActivity(Activity.playing("187")); // TODO MSG - Config/Command

        builder.enableIntents(GatewayIntent.GUILD_MEMBERS);
        builder.setChunkingFilter(ChunkingFilter.ALL);
        builder.setMemberCachePolicy(MemberCachePolicy.ALL);

        try {
            this.jda = builder.build();
        } catch (LoginException e) {
            BungeeMain.sendErrorMessage("DiscordBot could not be loaded");
            BungeeMain.shutdown();
        }

        this.registerListeners();

        try {
            this.jda.awaitReady();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        this.guild = this.jda.getGuildById(guildId);
        this.registerCommands(this.guild.updateCommands(), commands);
    }

    public void disableBot() {
        this.jda.getPresence().setStatus(OnlineStatus.OFFLINE);
        this.jda.shutdown();
    }

    public void setActivity(Activity activity) {
        this.jda.getPresence().setActivity(activity);
    }

    public void setStatus(OnlineStatus status) {
        this.jda.getPresence().setStatus(status);
    }

    private void registerListeners() {
        this.jda.addEventListener(new SlashCommandListener());
    }

    private void registerCommands(CommandListUpdateAction commandListUpdateAction, BSCommand[] commands) {
        for (BSCommand command : commands) {
            commandListUpdateAction.addCommands(command.getCommandData());
        }

        commandListUpdateAction.complete();
    }

    private void unregisterCommands(String... str) {
        for (String s : str) {
            this.getGuild().retrieveCommands().complete().stream().filter(command1 -> s.equals(command1.getName())).findFirst()
                    .ifPresent(command -> this.guild.deleteCommandById(command.getIdLong()).queue());
        }
    }

    public Guild getGuild() {
        return this.guild;
    }

    public JDA getJda() {
        return jda;
    }

}
