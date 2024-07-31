package org.royaldev.royalcommands.listeners;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.royaldev.royalcommands.Config;
import org.royaldev.royalcommands.RoyalCommands;
import org.royaldev.royalcommands.death.Death;

public class DeathListener implements Listener {

    private final RoyalCommands plugin;

    public DeathListener(final RoyalCommands instance) {
        this.plugin = instance;
    }

    private void sendDeathMessage(final String message, final World in) {
        final boolean interworld = Config.interworldDeathMessages;
        if (interworld) {
            this.plugin.getServer().broadcastMessage(message);
            return;
        }
        for (final Player p : this.plugin.getServer().getOnlinePlayers()) {
            if (!p.getWorld().getName().equals(in.getName())) continue;
            p.sendMessage(message);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(final PlayerDeathEvent event) {
        if (!Config.useCustomDeath) return;
        if (event.getEntity() == null) return;
        final World in = event.getEntity().getWorld();
        if (Config.ignoredDeathMessageWorlds.contains(in.getName())) return;
        if (Config.silencedDeathMessageWorlds.contains(in.getName())) {
            event.setDeathMessage(null);
            return;
        }
        event.setDeathMessage(null);
        final Death death = new Death(this.plugin, event);
        this.sendDeathMessage(death.getNewDeathMessage(), in);
    }

}
