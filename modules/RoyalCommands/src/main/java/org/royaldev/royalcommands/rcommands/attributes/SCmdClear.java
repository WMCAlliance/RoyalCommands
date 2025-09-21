/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands.attributes;

import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;
import org.royaldev.royalcommands.rcommands.CmdAttributes;
import org.royaldev.royalcommands.rcommands.SubCommand;

public class SCmdClear extends SubCommand<CmdAttributes> {

    public SCmdClear(final RoyalCommands instance, final CmdAttributes parent) {
        super(instance, parent, "clear", true, "Clear all attributes attached to the held item", "<command>", new String[0], new Short[0]);
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] eargs, final CommandArguments ca) {
        if (!this.getParent().validHand(cs))
            return true;

        final Player p = (Player) cs;
        ItemStack hand = p.getInventory().getItemInMainHand();
        ItemMeta meta = hand.getItemMeta();
        if (meta.hasAttributeModifiers()) {
            cs.sendMessage(MessageColor.POSITIVE + "Clearing Attributes of " + MessageColor.NEUTRAL + RUtils.getItemName(hand));
            for (Attribute a : meta.getAttributeModifiers().keys()) {
                meta.removeAttributeModifier(a);
            }
            hand.setItemMeta(meta);
            p.getInventory().setItemInMainHand(hand);
        } else {
            cs.sendMessage(MessageColor.NEGATIVE + "Item has no Attribute Modifiers applied!");
        }
        return true;
    }
}
