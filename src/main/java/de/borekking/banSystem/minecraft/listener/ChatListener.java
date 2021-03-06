package de.borekking.banSystem.minecraft.listener;

import de.borekking.banSystem.BungeeMain;
import de.borekking.banSystem.punishment.Platform;

import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ChatListener implements Listener {

    @EventHandler
    public void onChat(ChatEvent event) {
        Connection sender = event.getSender();

        // Check for mute
        if (!this.isMuted(sender)) {
            return;
        }

        // Check for Command
        if (event.isCommand()) {
            return;
        }

        // Don't send message since it's not
        // a command and the user is muted
        event.setCancelled(true);
    }

    private boolean isMuted(Connection connection) {
        if (!(connection instanceof ProxiedPlayer)) return false;
        ProxiedPlayer player = (ProxiedPlayer) connection;

        String uuidStr = player.getUniqueId().toString();
        long userID = BungeeMain.getUserID(Platform.MINECRAFT, uuidStr);
        if (userID < 0) return false;

        return BungeeMain.getInstance().getMuteHandler().isPunished(userID, Platform.MINECRAFT);
    }
}
