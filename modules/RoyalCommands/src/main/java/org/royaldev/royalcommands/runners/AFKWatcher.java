/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.runners;

import java.util.logging.Level;
import org.bukkit.entity.Player;
import org.royaldev.royalcommands.AFKUtils;
import org.royaldev.royalcommands.Config;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;

import java.util.logging.Logger;

// TODO: Make this sync vs async

public class AFKWatcher implements Runnable {

    private final RoyalCommands plugin;

    public AFKWatcher(RoyalCommands instance) {
        this.plugin = instance;
    }

    @Override
    public void run() {
        long afkKickTime = Config.afkKickTime;
        long afkAutoTime = Config.afkAutoTime;
        for (Player p : this.plugin.getServer().getOnlinePlayers()) {
            if (p == null) continue;
            final long currentTime = System.currentTimeMillis();
            if (!AFKUtils.isAfk(p)) {
                if (this.plugin.ah.isAuthorized(p, "rcmds.exempt.autoafk")) continue;
                if (this.plugin.isVanished(p)) continue;
                if (!AFKUtils.moveTimesContains(p)) continue;
                if (afkAutoTime <= 0) continue;
                final long lastMove = AFKUtils.getLastMove(p);
                if ((lastMove + (afkAutoTime * 1000)) < currentTime) {
                    AFKUtils.setAfk(p, currentTime);
                    this.plugin.getServer().broadcastMessage(RUtils.colorize(RUtils.replaceVars(Config.afkFormat, p)));
                }
                continue;
            }
            if (afkKickTime <= 0) continue;
            if (this.plugin.ah.isAuthorized(p, "rcmds.exempt.afkkick")) return;
            final long afkAt = AFKUtils.getAfkTime(p);
            if (afkAt + (afkKickTime * 1000) < currentTime) {
                try {
                    RUtils.scheduleKick(p, "You have been AFK for too long!");
                } catch (IllegalArgumentException | NullPointerException e) {
                    Logger.getLogger("Minecraft").log(Level.WARNING, "[RoyalCommands] Could not kick {0} for being AFK: {1}", new Object[]{p.getName(), e.getMessage()});
                }
            }
        }
    }

}
