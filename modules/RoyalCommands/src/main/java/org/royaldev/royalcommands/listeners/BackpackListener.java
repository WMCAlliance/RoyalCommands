/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.listeners;

import java.util.UUID;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.royaldev.royalcommands.Config;
import org.royaldev.royalcommands.RUtils;

public class BackpackListener implements Listener {

    @EventHandler
    public void backpackClearOnDeath(PlayerDeathEvent e) {
        if (!Config.backpackReset) return;
        final Player p = e.getEntity();
        final Inventory backpack = RUtils.getBackpack(p);
        backpack.clear();
        RUtils.saveBackpack(p, backpack);
    }

    @EventHandler
    public void backpackClose(InventoryCloseEvent e) {
        if (e.getInventory() == null || e.getView().getTitle() == null) return; // modpacks
        final InventoryHolder ih = e.getInventory().getHolder();
        if (!(ih instanceof BackpackHolder)) return;
        final BackpackHolder bh = (BackpackHolder) ih;
        final Inventory backpack = RUtils.getBackpack(bh.getOwnerUUID(), bh.getWorld());
        backpack.setContents(e.getInventory().getContents());
        RUtils.saveBackpack(bh.getOwnerUUID(), bh.getWorld(), backpack);
    }

    public static class BackpackHolder implements InventoryHolder {

        private final UUID ownerUUID;
        private final World w;

        public BackpackHolder(UUID ownerUUID, World w) {
            this.ownerUUID = ownerUUID;
            this.w = w;
        }

        /**
         * No function.
         *
         * @return Returns null
         * @deprecated No use
         */
        @Override
        @Deprecated
        public Inventory getInventory() {
            return null;
        }

        public UUID getOwnerUUID() {
            return this.ownerUUID;
        }

        public World getWorld() {
            return this.w;
        }
    }

}
