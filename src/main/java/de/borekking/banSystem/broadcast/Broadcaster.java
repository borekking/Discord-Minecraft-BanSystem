package de.borekking.banSystem.broadcast;

import de.borekking.banSystem.util.discord.DiscordUtils;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;

public class Broadcaster {

    // Class for broadcasting messages in a specified Discord Channel

    public Broadcaster() {
    }

    public void sendMessage( long channelID, MessageEmbed embed) {
        TextChannel textChannel = this.getTextChannel(channelID);
        if (textChannel == null) return;

        textChannel.sendMessageEmbeds(embed).queue();
    }

    public void sendMessage(long channelID, String msg) {
        TextChannel textChannel = this.getTextChannel(channelID);
        if (textChannel == null) return;

        textChannel.sendMessage(msg).queue();
    }

    public TextChannel getTextChannel(long channelID) {
        return DiscordUtils.getTextChannel(channelID);
    }
}
