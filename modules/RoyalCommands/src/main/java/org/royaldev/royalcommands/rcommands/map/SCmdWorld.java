/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands.map;

import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;
import org.royaldev.royalcommands.rcommands.CmdMap;
import org.royaldev.royalcommands.rcommands.SubCommand;


public class SCmdWorld extends SubCommand<CmdMap> {

    public SCmdWorld(final RoyalCommands instance, final CmdMap parent) {
        super(instance, parent, "world", true, "Change the world displayed by the map", "<command> (world)", new String[0], new Short[]{CompletionType.WORLD.getShort()});
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] eargs, final CommandArguments ca) {
        ItemStack hand = this.getParent().isMap(cs);
        if (hand == null)
            return true;

        if (eargs.length < 1) {
            cs.sendMessage(MessageColor.NEGATIVE + "Specify the world to set this map to.");
            return true;
        }
        Player p = (Player) cs;
        MapMeta mapMeta = (MapMeta) hand.getItemMeta();
        MapView mv = mapMeta.getMapView();
        String sworld = eargs[0];
        World w = this.plugin.getServer().getWorld(sworld);
        if (w == null) {
            cs.sendMessage(MessageColor.NEGATIVE + "No such world!");
            return true;
        }
        mv.setWorld(w);
        this.getParent().updateMap(p, mv);
        p.getInventory().setItemInMainHand(hand);
        cs.sendMessage(MessageColor.POSITIVE + "Set the world of map " + MessageColor.NEUTRAL + mv.getId() + MessageColor.POSITIVE + " to " + MessageColor.NEUTRAL + RUtils.getMVWorldName(w) + MessageColor.POSITIVE + ".");
        return true;
    }
}
