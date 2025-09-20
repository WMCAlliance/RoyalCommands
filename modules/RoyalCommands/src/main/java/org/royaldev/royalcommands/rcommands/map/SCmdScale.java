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
import org.bukkit.map.MapView.Scale;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;
import org.royaldev.royalcommands.rcommands.CmdMap;
import org.royaldev.royalcommands.rcommands.SubCommand;


public class SCmdScale extends SubCommand<CmdMap> {

    public SCmdScale(final RoyalCommands instance, final CmdMap parent) {
        super(instance, parent, "scale", true, "Set the scale of the map.", "<command> (scale)", new String[0], new Short[]{CompletionType.ENUM.getShort()});
    }

    @Override
    protected Scale[] customEnum(final CommandSender cs, final Command cmd, final String label, final String[] args, final String arg) {
        return Scale.values();
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] eargs, final CommandArguments ca) {
        ItemStack hand = this.getParent().isMap(cs);
        if (hand == null)
            return true;

        if (eargs.length < 1) {
            cs.sendMessage(this.getParent().combineEnums(Scale.values()));
            cs.sendMessage(MessageColor.NEGATIVE + "Please specify a scale.");
            return true;
        }
        Player p = (Player) cs;
        MapMeta mapMeta = (MapMeta) hand.getItemMeta();
        MapView mv = mapMeta.getMapView();
        String sscale = eargs[0].toUpperCase();
        Scale mvs;
        try {
            mvs = Scale.valueOf(sscale);
        } catch (IllegalArgumentException e) {
            cs.sendMessage(this.getParent().combineEnums(Scale.values()));
            cs.sendMessage(MessageColor.NEGATIVE + "Invalid scale type.");
            return true;
        }
        mv.setScale(mvs);
        this.getParent().updateMap(p, mv);
        p.getInventory().setItemInMainHand(hand);
        cs.sendMessage(MessageColor.POSITIVE + "Set the scale of map " + MessageColor.NEUTRAL + mv.getId() + MessageColor.POSITIVE + " to " + MessageColor.NEUTRAL + RUtils.getFriendlyEnumName(mvs) + MessageColor.POSITIVE + ".");
        return true;
    }
}
