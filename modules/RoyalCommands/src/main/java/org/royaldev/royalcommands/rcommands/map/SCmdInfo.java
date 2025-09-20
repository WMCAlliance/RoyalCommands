/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands.map;

import org.apache.commons.lang3.BooleanUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;
import org.royaldev.royalcommands.rcommands.CmdMap;
import org.royaldev.royalcommands.rcommands.SubCommand;


public class SCmdInfo extends SubCommand<CmdMap> {

    public SCmdInfo(final RoyalCommands instance, final CmdMap parent) {
        super(instance, parent, "info", true, "Display information about the map", "<command>", new String[0], new Short[0]);
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] eargs, final CommandArguments ca) {
        ItemStack hand = this.getParent().isMap(cs);
        if (hand == null)
            return true;

        MapMeta mapMeta = (MapMeta) hand.getItemMeta();
        MapView mv = mapMeta.getMapView();
        cs.sendMessage(MessageColor.POSITIVE + "Information about map " + MessageColor.NEUTRAL + mv.getId() + MessageColor.POSITIVE + ":");
        cs.sendMessage("  " + MessageColor.POSITIVE + "Center coordinates: " + MessageColor.NEUTRAL + mv.getCenterX() + MessageColor.POSITIVE + ", " + MessageColor.NEUTRAL + mv.getCenterZ());
        cs.sendMessage("  " + MessageColor.POSITIVE + "World: " + MessageColor.NEUTRAL + RUtils.getMVWorldName(mv.getWorld()));
        cs.sendMessage("  " + MessageColor.POSITIVE + "Scale: " + MessageColor.NEUTRAL + RUtils.getFriendlyEnumName(mv.getScale()));
        cs.sendMessage("  " + MessageColor.POSITIVE + "Locked: " + MessageColor.NEUTRAL + mv.isLocked());
        cs.sendMessage("  " + MessageColor.POSITIVE + "Tracking: " + MessageColor.NEUTRAL + mv.isTrackingPosition());
        cs.sendMessage("  " + MessageColor.POSITIVE + "Unlimited Tracking: " + MessageColor.NEUTRAL + mv.isUnlimitedTracking());
        cs.sendMessage("  " + MessageColor.POSITIVE + "Virtual: " + MessageColor.NEUTRAL + BooleanUtils.toStringYesNo(mv.isVirtual()));
        //cs.sendMessage("  " + MessageColor.POSITIVE + "Char: " + MessageColor.NEUTRAL + "stuff");
        return true;
    }
}
