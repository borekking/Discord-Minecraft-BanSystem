package de.borekking.banSystem.util.discord;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.Color;
import java.time.OffsetDateTime;
import java.util.Map;

public class MyEmbedBuilder {

    private final EmbedBuilder embed;

    public MyEmbedBuilder() {
        this.embed = new EmbedBuilder();
    }

    public MyEmbedBuilder field(String name, String value, boolean inline) {
        this.embed.addField(name, value, inline);
        return this;
    }

    public MyEmbedBuilder field(Map<String, String> fields, boolean inline) {
        fields.forEach((name, value) -> this.field(name, value, inline));
        return this;
    }

    public MyEmbedBuilder description(String description) {
        this.embed.setDescription(description);
        return this;
    }

    public MyEmbedBuilder color(Color color) {
        this.embed.setColor(color);
        return this;
    }

    public MyEmbedBuilder title(String title) {
        this.embed.setTitle(title);
        return this;
    }

    public MyEmbedBuilder title(String title, String url) {
        this.embed.setTitle(title, url);
        return this;
    }

    public MyEmbedBuilder title(OffsetDateTime timestamp) {
        this.embed.setTimestamp(timestamp);
        return this;
    }

    public MyEmbedBuilder thumbnail(String url) {
        this.embed.setThumbnail(url);
        return this;
    }

    public MyEmbedBuilder author(String author) {
        this.embed.setAuthor(author);
        return this;
    }

    public MyEmbedBuilder footer(String footer) {
        this.embed.setFooter(footer);
        return this;
    }

    public MyEmbedBuilder image(String image) {
        this.embed.setImage(image);
        return this;
    }

    public MessageEmbed build() {
        return this.embed.build();
    }
}

