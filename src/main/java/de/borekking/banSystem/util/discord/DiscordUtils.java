package de.borekking.banSystem.util.discord;

import de.borekking.banSystem.BungeeMain;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

public class DiscordUtils {

    public static User getUser(Long id) {
        return BungeeMain.getInstance().getDiscordBot().getJda().getUserById(id);
    }

    public static User getUser(String id) {
        return getUser(Long.parseLong(id));
    }

    public static Role getRole(Long id) {
        Guild guild = BungeeMain.getInstance().getDiscordBot().getGuild();
        return guild.getRoleById(id);
    }

    public static Role getRole(String id) {
        return getRole(Long.parseLong(id));
    }

    public static void ban(User user, int delDays, String reason) {
        Guild guild = BungeeMain.getInstance().getDiscordBot().getGuild();
        guild.ban(user, delDays, reason).queue();
    }

    public static void ban(User user) {
        ban(user, 7, "");
    }

    public static void unban(User user) {
        Guild guild = BungeeMain.getInstance().getDiscordBot().getGuild();
        guild.unban(user).queue();
    }

    public static void addRole(User user, Role role) {
        Guild guild = BungeeMain.getInstance().getDiscordBot().getGuild();
        guild.addRoleToMember(user, role).queue();
    }

    public static void removeRole(User user, Role role) {
        Guild guild = BungeeMain.getInstance().getDiscordBot().getGuild();
        guild.removeRoleFromMember(user, role).queue();
    }
}