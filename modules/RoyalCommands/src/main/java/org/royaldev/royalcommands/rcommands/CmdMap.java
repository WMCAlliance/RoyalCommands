/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RoyalCommands;
import org.royaldev.royalcommands.rcommands.map.SCmdInfo;
import org.royaldev.royalcommands.rcommands.map.SCmdLock;
import org.royaldev.royalcommands.rcommands.map.SCmdPosition;
import org.royaldev.royalcommands.rcommands.map.SCmdScale;
import org.royaldev.royalcommands.rcommands.map.SCmdTrack;
import org.royaldev.royalcommands.rcommands.map.SCmdUnlimited;
import org.royaldev.royalcommands.rcommands.map.SCmdWorld;

@ReflectCommand
public class CmdMap extends ParentCommand {

    public CmdMap(final RoyalCommands instance, final String name) {
        super(instance, name, true);
        this.addSubCommand(new SCmdScale(this.plugin, this));
        this.addSubCommand(new SCmdInfo(this.plugin, this));
        this.addSubCommand(new SCmdLock(this.plugin, this));
        this.addSubCommand(new SCmdPosition(this.plugin, this));
        this.addSubCommand(new SCmdTrack(this.plugin, this));
        this.addSubCommand(new SCmdUnlimited(this.plugin, this));
        this.addSubCommand(new SCmdWorld(this.plugin, this));
    }

    public String combineEnums(Enum[] es) {
        StringBuilder sb = new StringBuilder();
        for (Enum e : es) {
            sb.append(MessageColor.NEUTRAL);
            sb.append(e.name());
            sb.append(MessageColor.RESET);
            sb.append(", ");
        }
        return sb.substring(0, sb.length() - 4);
    }

    public void updateMap(Player p, MapView mv) {
        p.sendMap(mv);
    }

    public ItemStack isMap(final CommandSender cs) {
        if (!(cs instanceof Player)) {
            cs.sendMessage(MessageColor.NEGATIVE + "This command is only available to players!");
            return null;
        }
        Player p = (Player) cs;
        ItemStack hand = p.getInventory().getItemInMainHand();
        if (hand == null || hand.getType() != Material.FILLED_MAP) {
            cs.sendMessage(MessageColor.NEGATIVE + "You must be holding a filled map!");
            return null;
        }
        MapMeta mapMeta = (MapMeta) hand.getItemMeta();
        MapView mv = mapMeta.getMapView();
        if (mv == null) {
            cs.sendMessage(MessageColor.NEGATIVE + "This map seems to be corrupted/invalid.");
            return null;
        }
        return hand;
    }
}
