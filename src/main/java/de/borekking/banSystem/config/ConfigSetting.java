package de.borekking.banSystem.config;

import de.borekking.banSystem.BungeeMain;

import java.util.function.Function;

import net.md_5.bungee.api.ChatColor;

public enum ConfigSetting {

    // Enum storing config values

    // Minecraft
    MINECRAFT_PREFIX("minecraft.prefix", "&7[&cBanSystem&7]"),

    // Discord Bot
    MUTE_ROLE("discord.muteRole", -1),
    DISCORD_TOKEN("discord.token",""),
    DISCORD_GUILD_ID("discord.guildID",""),

    // SQL
    SQL_HOST("sql.host",""),
    SQL_DATABASE("sql.database",""),
    SQL_USER("sql.user",""),
    SQL_PASSWORD("sql.password",""),

    SQL_PORT("sql.port", ""),

    SQL_TYPE("sql.type", "mysql"),

    // Ban (Messages)
    BAN_DISCORD_MESSAGE("ban.discord.message", "Banned for reason: %reason%"),
    BAN_MINECRAFT_MESSAGE("ban.minecraft.message", "You were banned!\n %reason%"),

    BAN_BROADCAST_CHANNEL("ban.broadcastChannel", -1L),

    // Mute (Messages)
    MUTE_DISCORD_MUTE("mute.discord.mute.message", ""),
    MUTE_DISCORD_UNMUTE("mute.discord.unmute.message", ""),

    MUTE_MINECRAFT_MUTE("mute.minecraft.mute.message", ""),
    MUTE_MINECRAFT_UNMUTE("mute.minecraft.unmute.message", ""),
    MUTE_BROADCAST_CHANNEL("mute.broadcastChannel", -1L);

    private final String path; // Key
    private Object value; // Value

    ConfigSetting(String path, Object value) {
        this.path = path;
        this.value = value;
    }

    // Convert value with function and catch ClassCastExceptions
    private <T> T container(Function<Object, T> f) {
        // Prevent NPE
        if (this.value == null) return null;

        try {
            return f.apply(this.value);
        } catch (ClassCastException e) {
            BungeeMain.sendErrorMessage("ERROR WHILE TRYING TO GET CONFIG VALUE! PLEASE CHECK YOUR CONFIG!");
            return null;
        }
    }

    // Path/Key getter
    public String getPath() {
        return this.path;
    }

    // Getters returning the value in different types:

    public Object getValue() {
        return this.value;
    }

    public boolean getValueAsBoolean() {
        return Boolean.TRUE.equals(this.container(o -> (boolean) o));
    }

    public String getValueAsString() {
        return this.container(String::valueOf);
    }

    public String getValueAsColorString() {
        String stringValue = this.container(o -> (String) o);
        if (stringValue == null) return null;

        return ChatColor.translateAlternateColorCodes('&', stringValue);
    }

    public Number getValueAsNumber() {
        return this.container(o -> (Number) o);
    }

    // Default value for Numbers (value variables): 0

    public double getValueAsDouble() {
        Number number = this.getValueAsNumber();
        if (number == null) return 0D;

        return number.doubleValue();
    }

    public int getValueAsInt() {
        Number number = this.getValueAsNumber();
        if (number == null) return 0;

        return number.intValue();
    }

    public long getValueAsLong() {
        Number number = this.getValueAsNumber();
        if (number == null) return 0L;

        return number.longValue();
    }

    // Value setter
    public void setValue(Object value) {
        this.value = value;
    }
}
