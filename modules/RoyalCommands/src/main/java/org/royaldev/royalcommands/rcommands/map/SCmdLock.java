/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands.map;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RoyalCommands;
import org.royaldev.royalcommands.rcommands.CmdMap;
import org.royaldev.royalcommands.rcommands.SubCommand;


public class SCmdLock extends SubCommand<CmdMap> {

    public SCmdLock(final RoyalCommands instance, final CmdMap parent) {
        super(instance, parent, "lock", true, "Toggle the lock status of the map", "<command>", new String[0], new Short[0]);
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] eargs, final CommandArguments ca) {
        ItemStack hand = this.getParent().isMap(cs);
        if (hand == null)
            return true;

        Player p = (Player) cs;
        MapMeta mapMeta = (MapMeta) hand.getItemMeta();
        MapView mv = mapMeta.getMapView();
        Boolean newLock = !mv.isLocked();
        mv.setLocked(newLock);
        cs.sendMessage(MessageColor.POSITIVE + "Map " + MessageColor.NEUTRAL + mv.getId() + MessageColor.POSITIVE + " toggled to " + MessageColor.NEUTRAL + (newLock ? "locked" : "unlocked") + MessageColor.POSITIVE + ".");
        this.getParent().updateMap(p, mv);
        p.getInventory().setItemInMainHand(hand);
        return true;
    }
}
