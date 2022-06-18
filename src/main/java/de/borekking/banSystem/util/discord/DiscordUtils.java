package de.borekking.banSystem.util.discord;

import de.borekking.banSystem.BungeeMain;

import java.util.stream.Collectors;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

public class DiscordUtils {

    public static User getUserByID(Long id) {
        return BungeeMain.getInstance().getDiscordBot().getJda().getUserById(id);
    }

    public static User getUserByID(String id) {
        try {
            return getUserByID(Long.parseLong(id));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // E.g. "borekking#0187"
    public static User getUserByTag(String tag) {
        try {
            return BungeeMain.getInstance().getDiscordBot().getJda().getUserByTag(tag);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static Member getMember(Guild guild, User user) {
        return guild.getMember(user);
    }

    public static Role getRole(Long id) {
        Guild guild = BungeeMain.getInstance().getDiscordBot().getGuild();
        return guild.getRoleById(id);
    }

    public static Role getRole(String id) {
        return getRole(Long.parseLong(id));
    }

    // Return if user was actually banned.
    public static boolean ban(User user, int delDays, String reason) {
        Guild guild = BungeeMain.getInstance().getDiscordBot().getGuild();

        try {
            guild.ban(user, delDays, reason).queue();
        } catch (IllegalArgumentException e) {
            return false;
        }

        return true;
    }

    // Return if user was actually banned.
    public static boolean ban(User user) {
        return ban(user, 7, "");
    }

    // Return if user was banned
    public static boolean unban(final User user) {
        final Guild guild = BungeeMain.getInstance().getDiscordBot().getGuild();

        if (!userIsBanned(user)) return false;

        guild.unban(user).queue();
        return true;
    }

    public static boolean userIsBanned(final User user) {
        final Guild guild = BungeeMain.getInstance().getDiscordBot().getGuild();

        boolean[] b = new boolean[1];

        guild.retrieveBanList().queue(list -> b[0] = list.stream()
                .map(Guild.Ban::getUser).collect(Collectors.toList()).contains(user));

        return b[0];
    }

    public static void addRole(User user, Role role) {
        Guild guild = BungeeMain.getInstance().getDiscordBot().getGuild();
        guild.addRoleToMember(user, role).queue();
    }

    public static void removeRole(User user, Role role) {
        Guild guild = BungeeMain.getInstance().getDiscordBot().getGuild();
        guild.removeRoleFromMember(user, role).queue();
    }

    public static TextChannel getTextChannel(long id) {
        Guild guild = BungeeMain.getInstance().getGuild();

        return guild.getTextChannelById(id);
    }
}
