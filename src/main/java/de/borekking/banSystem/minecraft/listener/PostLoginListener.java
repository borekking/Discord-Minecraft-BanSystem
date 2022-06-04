package de.borekking.banSystem.minecraft.listener;

import de.borekking.banSystem.BungeeMain;
import de.borekking.banSystem.punishment.Platform;
import de.borekking.banSystem.punishment.Punishment;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PostLoginListener implements Listener {

    @EventHandler
    public void onPostLogin(PostLoginEvent event) {
        ProxiedPlayer player = event.getPlayer();

        // Check for ban
        long userID = this.getUserID(player);
        if (userID < 0) return;

        Punishment ban = this.getBan(userID);
        if (ban == null) return;

        if (this.isBanned(ban)) {
            // TODO Custom message
            player.disconnect(new TextComponent("You are banned!\n"), new TextComponent(ban.getReason()));
        }
    }

    private long getUserID(ProxiedPlayer player) {
        String uuidStr = player.getUniqueId().toString();
        return BungeeMain.getUserID(Platform.MINECRAFT, uuidStr);
    }

    private Punishment getBan(long userID) {
        if (userID < 0) return null;

        return BungeeMain.getInstance().getBanHandler().getPunishment(userID, Platform.MINECRAFT);
    }

    private boolean isBanned(Punishment punishment) {
        return !BungeeMain.getInstance().getBanHandler().isOver(punishment);
    }
}
