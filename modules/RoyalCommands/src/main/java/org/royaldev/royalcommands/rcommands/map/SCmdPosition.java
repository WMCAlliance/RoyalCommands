/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands.map;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Location;
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


public class SCmdPosition extends SubCommand<CmdMap> {

    public SCmdPosition(final RoyalCommands instance, final CmdMap parent) {
        super(instance, parent, "position", true, "Set the center position of the map", "<command> (x) (z)", new String[0], new Short[]{CompletionType.LIST.getShort()});
    }

    @Override
    protected List<String> customList(final CommandSender cs, final Command cmd, final String label, final String[] args, final String arg) {
        if (!(cs instanceof Player))
            return new ArrayList<>();
        Player p = (Player) cs;
        Location pLoc = p.getLocation();
        Location wLoc = p.getWorld().getSpawnLocation();
        return Arrays.asList(
                pLoc.getBlockX() + " " + pLoc.getBlockZ(),
                wLoc.getBlockX() + " " + wLoc.getBlockZ());
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] eargs, final CommandArguments ca) {
        ItemStack hand = this.getParent().isMap(cs);
        if (hand == null)
            return true;

        if (eargs.length < 2) {
            cs.sendMessage(MessageColor.NEGATIVE + "Please specify the new X and Z coordinates for the center of the map.");
            return true;
        }
        Player p = (Player) cs;
        MapMeta mapMeta = (MapMeta) hand.getItemMeta();
        MapView mv = mapMeta.getMapView();
        int x, z;
        try {
            x = Integer.parseInt(eargs[0]);
            z = Integer.parseInt(eargs[1]);
        } catch (NumberFormatException e) {
            cs.sendMessage(MessageColor.NEGATIVE + "Those coordinates were invalid!");
            return true;
        }
        mv.setCenterX(x);
        mv.setCenterZ(z);
        this.getParent().updateMap(p, mv);
        p.getInventory().setItemInMainHand(hand);
        cs.sendMessage(MessageColor.POSITIVE + "Set the center of map " + MessageColor.NEUTRAL + mv.getId() + MessageColor.POSITIVE + " to " + MessageColor.NEUTRAL + x + MessageColor.POSITIVE + ", " + MessageColor.NEUTRAL + z + MessageColor.POSITIVE + ".");
        return true;
    }
}
