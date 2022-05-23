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
        if (this.isMuted(sender)) {
            event.setCancelled(true);
        }
    }

    private boolean isMuted(Connection connection) {
        if (!(connection instanceof ProxiedPlayer)) return false;
        ProxiedPlayer player = (ProxiedPlayer) connection;

        String uuidStr = player.getUniqueId().toString();
        long userID = BungeeMain.getInstance().getUserManager().getUserID(Platform.MINECRAFT, uuidStr);
        if (userID < 0) return false;

        return BungeeMain.getInstance().getBanHandler().isPunished(userID, Platform.MINECRAFT);
    }
}
